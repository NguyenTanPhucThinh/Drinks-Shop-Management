package com.uitdrinkapp.app;

import android.content.Context;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.uitdrinkapp.app.data.model.ShopSession;
import com.uitdrinkapp.app.data.repository.FakeShopRepository;
import com.uitdrinkapp.app.data.repository.PreferencesShopSessionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class HomeJoinFlowTest {
    private Context context;
    @Before public void resetSession() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.getSharedPreferences("shop_session", Context.MODE_PRIVATE).edit().clear().commit();
        InstrumentationRegistry.getInstrumentation().getUiAutomation().grantRuntimePermission(
                context.getPackageName(), android.Manifest.permission.CAMERA);
    }
    @Test public void manualEntryPersistsAndBackDoesNotChangeSelection() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.tv_shop_status)).check(matches(withText(R.string.home_not_joined)));
            onView(withId(R.id.btn_scan_qr)).perform(click());
            onView(withId(R.id.et_shop_code)).perform(scrollTo(), replaceText("ts-0123"), closeSoftKeyboard());
            onView(withId(R.id.et_table_number)).perform(scrollTo(), replaceText("05"), closeSoftKeyboard());
            scenario.recreate();
            onView(withId(R.id.et_table_number)).check(matches(withText("05")));
            onView(withId(R.id.btn_join)).perform(scrollTo(), click());
            onView(withId(R.id.tv_shop_status)).check(matches(withText(context.getString(R.string.home_joined_table, "05"))));
            onView(withId(R.id.btn_scan_qr)).perform(click());
            onView(withId(R.id.et_table_number)).perform(scrollTo(), replaceText("99"), closeSoftKeyboard());
            pressBack();
            onView(withId(R.id.tv_shop_status)).check(matches(withText(context.getString(R.string.home_joined_table, "05"))));
        }
        try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.tv_shop_status)).check(matches(withText(context.getString(R.string.home_joined_table, "05"))));
        }
        ShopSession persisted = new PreferencesShopSessionRepository(context, new FakeShopRepository()).getSession();
        assertNotNull(persisted);
        assertEquals("05", persisted.getTableNumber());
    }
    @Test public void cameraScreenAndUnavailableActions() {
        try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.btn_scan_qr)).perform(click());
            onView(withId(R.id.preview_camera)).check(matches(isDisplayed()));
            pressBack();
            onView(withId(R.id.tv_shop_status)).check(matches(withText(R.string.home_not_joined)));
            onView(withId(R.id.btn_nav_menu)).perform(click());
            onView(withText(R.string.home_unavailable)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_scan_qr)).check(matches(isDisplayed()));
        }
    }
    @Test public void captureScreens() throws Exception {
        try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.btn_scan_qr)).check(matches(isDisplayed()));
            capture("home");
            onView(withId(R.id.btn_scan_qr)).perform(click());
            onView(withId(R.id.preview_camera)).check(matches(isDisplayed()));
            java.util.concurrent.CountDownLatch streaming = new java.util.concurrent.CountDownLatch(1);
            ignored.onActivity(activity -> {
                androidx.camera.view.PreviewView preview = activity.findViewById(R.id.preview_camera);
                preview.getPreviewStreamState().observe(activity, value -> {
                    if (value == androidx.camera.view.PreviewView.StreamState.STREAMING) streaming.countDown();
                });
            });
            assertTrue(streaming.await(20, java.util.concurrent.TimeUnit.SECONDS));
            capture("join");
        }
    }
    private void capture(String name) throws Exception {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        android.graphics.Bitmap bitmap = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        java.io.File directory = new java.io.File(context.getExternalFilesDir(null), "screenshots");
        if (!directory.exists() && !directory.mkdirs()) throw new java.io.IOException("Cannot create screenshots directory");
        try (java.io.FileOutputStream output = new java.io.FileOutputStream(new java.io.File(directory, name + ".png"))) {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output);
        }
        bitmap.recycle();
    }
    @Test public void removedShopReturnsToNotJoined() {
        context.getSharedPreferences("shop_session", Context.MODE_PRIVATE).edit().putString("shop_id", "removed").commit();
        assertNull(new PreferencesShopSessionRepository(context, new FakeShopRepository()).getSession());
    }
}
