package com.uitdrinkapp.app.data.mock;

import com.uitdrinkapp.app.data.model.Banner;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Mock content shown on the home screen. */
public final class HomeMockData {
    private HomeMockData() { }
    public static final List<String> FEATURED_IDS = Collections.unmodifiableList(Arrays.asList("milk_tea", "matcha"));
    public static final List<String> RECOMMENDED_IDS = Collections.unmodifiableList(Arrays.asList("peach_tea", "salt_coffee"));
    public static final List<Banner> BANNERS = Collections.unmodifiableList(Arrays.asList(
            new Banner("home_banner_tea", "Giảm 50% đồ uống", "Khám phá voucher của bạn"),
            new Banner("home_banner_peach", "Mua 1 tặng 1", "Thêm niềm vui, thêm ưu đãi")));
}
