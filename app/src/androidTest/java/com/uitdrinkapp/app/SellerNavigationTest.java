package com.uitdrinkapp.app;

import android.graphics.Bitmap;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.SystemClock;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.uitdrinkapp.app.ui.seller.main.SellerMainActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public final class SellerNavigationTest {
    private static final int[] TABS = {
            R.id.ll_seller_nav_home, R.id.ll_seller_nav_notification,
            R.id.ll_seller_nav_orders, R.id.ll_seller_nav_product,
            R.id.ll_seller_nav_other_features, R.id.ll_seller_nav_profile
    };
    private static final int[] TITLES = {
            R.id.tv_seller_home_title, R.id.tv_seller_notification_title,
            R.id.tv_seller_orders_title, R.id.tv_seller_product_title,
            R.id.tv_seller_other_features_title, R.id.tv_seller_profile_title
    };
    private static final int[] TEXTS = {
            R.string.seller_title_home, R.string.seller_title_notification,
            R.string.seller_title_orders, R.string.seller_title_product,
            R.string.seller_title_other_features, R.string.seller_title_profile
    };
    private static final String[] SCREENSHOTS = {
            "home", "notification", "orders", "product", "other-features", "profile"
    };

    private void checkTab(int active) {
        onView(withId(TITLES[active])).check(matches(isCompletelyDisplayed()));
        onView(withId(TITLES[active])).check(matches(withText(TEXTS[active])));
        for (int i = 0; i < TABS.length; i++) {
            onView(withId(TABS[i])).check(matches(isCompletelyDisplayed()));
            onView(withId(TABS[i])).check(matches(i == active ? isSelected() : not(isSelected())));
        }
    }

    @Test
    public void allSixTabsShowOnlyTheirTitleAndDoNotAccumulateHistory() throws Exception {
        try (ActivityScenario<SellerMainActivity> scenario =
                     ActivityScenario.launch(SellerMainActivity.class)) {
            checkTab(0);
            for (int i = 0; i < TABS.length; i++) {
                onView(withId(TABS[i])).perform(click());
                onView(withId(TABS[i])).perform(click());
                checkTab(i);
                scenario.onActivity(activity -> {
                    NavHostFragment host = (NavHostFragment) activity.getSupportFragmentManager()
                            .findFragmentById(R.id.fcv_seller_nav_host);
                    assertEquals(1, host.getChildFragmentManager().getFragments().size());
                    Fragment screen = host.getChildFragmentManager().getPrimaryNavigationFragment();
                    assertTrue(screen.requireView() instanceof android.widget.TextView);
                });
                saveScreenshot(SCREENSHOTS[i]);
            }
            pressBack();
            checkTab(0);
            for (int i = 1; i < TABS.length; i++) {
                onView(withId(TABS[i])).perform(click());
                checkTab(i);
                pressBack();
                checkTab(0);
            }
            CountDownLatch destroyed = new CountDownLatch(1);
            scenario.onActivity(activity -> activity.getLifecycle().addObserver(
                    (androidx.lifecycle.LifecycleEventObserver) (source, event) -> {
                        if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY) {
                            destroyed.countDown();
                        }
                    }));
            pressBackUnconditionally();
            assertTrue("Back at Home should finish the activity", destroyed.await(5, TimeUnit.SECONDS));
            assertEquals(androidx.lifecycle.Lifecycle.State.DESTROYED, scenario.getState());
        }
    }

    @Test
    public void recreationRestoresDestinationAndSelection() {
        try (ActivityScenario<SellerMainActivity> scenario =
                     ActivityScenario.launch(SellerMainActivity.class)) {
            onView(withId(R.id.ll_seller_nav_orders)).perform(click());
            scenario.recreate();
            checkTab(2);
            onView(withId(R.id.ll_seller_nav_profile)).perform(click());
            scenario.recreate();
            checkTab(5);
            onView(withId(R.id.ll_seller_nav_home)).perform(click());
            checkTab(0);
            scenario.recreate();
            checkTab(0);
        }
    }

    @Test
    public void rotationKeepsSelectedTabAndNavbarVisible() throws Exception {
        try (ActivityScenario<SellerMainActivity> scenario =
                     ActivityScenario.launch(SellerMainActivity.class)) {
            onView(withId(R.id.ll_seller_nav_orders)).perform(click());
            scenario.onActivity(activity -> activity.setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
            awaitOrientation(scenario, Configuration.ORIENTATION_LANDSCAPE);
            checkTab(2);
            saveScreenshot("orders-landscape");
            scenario.onActivity(activity -> activity.setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
            awaitOrientation(scenario, Configuration.ORIENTATION_PORTRAIT);
            checkTab(2);
        }
    }

    private void awaitOrientation(ActivityScenario<SellerMainActivity> scenario, int expected) {
        long deadline = SystemClock.uptimeMillis() + 5000;
        AtomicBoolean matches = new AtomicBoolean(false);
        do {
            scenario.onActivity(activity -> matches.set(
                    activity.getResources().getConfiguration().orientation == expected));
            if (matches.get()) return;
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() < deadline);
        assertTrue("Requested orientation should be applied", matches.get());
    }

    @Test
    public void customerHomeShortcutOpensSellerAndBackReturnsToCustomer() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.btn_home_seller_preview)).check(matches(isCompletelyDisplayed()));
            onView(withId(R.id.btn_home_seller_preview)).perform(click());
            checkTab(0);
            pressBack();
            onView(withId(R.id.btn_home_seller_preview)).check(matches(isCompletelyDisplayed()));
            onView(withId(R.id.tv_greeting)).check(matches(isDisplayed()));
        }
    }

    private void saveScreenshot(String name) throws Exception {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        // Ripples run on the render thread after Espresso considers the UI idle.
        SystemClock.sleep(350);
        Bitmap bitmap = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        File directory = new File(InstrumentationRegistry.getInstrumentation()
                .getTargetContext().getExternalFilesDir(null), "screenshots");
        if (!directory.exists() && !directory.mkdirs()) {
            throw new java.io.IOException("Cannot create screenshot directory");
        }
        try (FileOutputStream output = new FileOutputStream(new File(directory, "seller-" + name + ".png"))) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
        } finally {
            bitmap.recycle();
        }
    }
}
