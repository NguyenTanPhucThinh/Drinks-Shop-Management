package com.uitdrinkapp.app.ui.common;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.DrawableRes;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.uitdrinkapp.app.R;

public final class LocalImages {
    private LocalImages() { }
    /** Khoá ảnh do người bán tải lên là địa chỉ (content://, file://, http...), không phải khoá có sẵn. */
    public static boolean isUri(String key) {
        return key != null && (key.startsWith("content://") || key.startsWith("file://")
                || key.startsWith("http://") || key.startsWith("https://"));
    }

    /** Nạp ảnh sản phẩm: địa chỉ tải lên thì nạp theo Uri, ngược lại dùng ảnh có sẵn trong app. */
    public static RequestBuilder<Drawable> load(RequestManager glide, String key) {
        return isUri(key) ? glide.load(Uri.parse(key)) : glide.load(resolve(key));
    }

    @DrawableRes public static int resolve(String key) {
        if (key == null) return R.drawable.home_milk_tea;
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
