package com.uitdrinkapp.app;

import android.graphics.Bitmap;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ProductDetailTest {
    @Test public void homeProductOpensDetailAndOptionsSurviveRecreation() throws Exception {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withText("Trà sữa trân châu")).perform(scrollTo(), click());
            onView(withId(R.id.tv_detail_name)).check(matches(withText("Trà sữa trân châu")));
            onView(withId(R.id.btn_detail_buy)).check(matches(withText("Đặt ngay · 40.000đ")));
            onView(withId(R.id.btn_quantity_minus)).check(matches(not(isEnabled())));
            scenario.onActivity(activity -> assertEquals(View.GONE, activity.findViewById(R.id.ll_bottom_nav).getVisibility()));
            capture();
            onView(withId(R.id.chip_size_m)).perform(scrollTo(), click());
            onView(withText("Pudding trứng")).perform(scrollTo(), click());
            onView(withId(R.id.btn_quantity_plus)).perform(scrollTo(), click());
            onView(withId(R.id.btn_detail_buy)).check(matches(withText("Đặt ngay · 104.000đ")));
            onView(withId(R.id.chip_hot)).perform(scrollTo(), click());
            onView(withId(R.id.chip_sugar_30)).perform(scrollTo(), click());
            onView(withId(R.id.chip_takeaway)).perform(scrollTo(), click());
            scenario.recreate();
            onView(withId(R.id.btn_detail_buy)).check(matches(withText("Đặt ngay · 104.000đ")));
            onView(withText("Pudding trứng")).check(matches(isChecked()));
            onView(withText("Trân châu đen")).check(matches(isChecked()));
            onView(withId(R.id.chip_hot)).check(matches(isChecked()));
            onView(withId(R.id.chip_takeaway)).check(matches(isChecked()));
            onView(withId(R.id.btn_detail_add_cart)).perform(click());
            onView(withText(R.string.detail_cart_pending)).check(matches(isDisplayed()));
            pressBack();
            onView(withId(R.id.vp_banners)).check(matches(isDisplayed()));
        }
    }
    private void capture() throws Exception {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Bitmap bitmap = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        File folder = new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null), "screenshots");
        if (!folder.exists() && !folder.mkdirs()) throw new java.io.IOException("Cannot create screenshot directory");
        try (FileOutputStream file = new FileOutputStream(new File(folder, "product-detail.png"))) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, file);
        }
        bitmap.recycle();
    }
}
