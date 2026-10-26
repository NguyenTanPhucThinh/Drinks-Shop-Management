package com.uitdrinkapp.app;

import android.graphics.Bitmap;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class VoucherNavigationTest {
    @Test public void bannerOpensVouchersAndNavigationStaysConsistent() throws Exception {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.vp_banners)).perform(click());
            onView(withId(R.id.rv_vouchers)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_nav_vouchers)).check(matches(isSelected()));
            onView(withId(R.id.btn_nav_home)).check(matches(org.hamcrest.Matchers.not(isSelected())));
            scenario.onActivity(activity -> {
                RecyclerView list = activity.findViewById(R.id.rv_vouchers);
                assertEquals(4, list.getAdapter().getItemCount());
            });
            scenario.recreate();
            onView(withId(R.id.btn_nav_vouchers)).check(matches(isSelected()));
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            Bitmap image = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
            File folder = new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null), "screenshots");
            if (!folder.exists() && !folder.mkdirs()) throw new java.io.IOException("Cannot create screenshot directory");
            try (FileOutputStream file = new FileOutputStream(new File(folder, "vouchers.png"))) {
                image.compress(Bitmap.CompressFormat.PNG, 100, file);
            }
            image.recycle();
            onView(withId(R.id.btn_voucher_back)).perform(click());
            onView(withId(R.id.vp_banners)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_nav_home)).check(matches(isSelected()));
            onView(withId(R.id.btn_nav_vouchers)).perform(click());
            onView(withId(R.id.btn_nav_vouchers)).perform(click());
            pressBack();
            onView(withId(R.id.vp_banners)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_nav_vouchers)).perform(click());
            onView(withId(R.id.btn_nav_home)).perform(click());
            onView(withId(R.id.vp_banners)).check(matches(isDisplayed()));
        }
    }
}
