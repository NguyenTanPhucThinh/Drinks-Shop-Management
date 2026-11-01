package com.uitdrinkapp.app.ui.common;
import androidx.annotation.DrawableRes;
import com.uitdrinkapp.app.R;

public final class LocalImages {
    private LocalImages() { }
    @DrawableRes public static int resolve(String key) {
        switch (key) {
            case "home_matcha": return R.drawable.home_matcha;
            case "home_peach_tea": return R.drawable.home_peach_tea;
            case "home_salt_coffee": return R.drawable.home_salt_coffee;
            case "home_banner_tea": return R.drawable.home_banner_tea;
            case "home_banner_peach": return R.drawable.home_banner_peach;
            default: return R.drawable.home_milk_tea;
        }
    }
}
