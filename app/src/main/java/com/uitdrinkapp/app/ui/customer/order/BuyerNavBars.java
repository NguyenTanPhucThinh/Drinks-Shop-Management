package com.uitdrinkapp.app.ui.customer.order;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Điều chỉnh bottom navigation của host, nếu host có dùng Material BottomNavigationView.
 * Màn 06/08/09/11 ẩn; màn 10 hiện. Không đụng file MainActivity của nhóm.
 */
public final class BuyerNavBars {
    private BuyerNavBars() { }
    public static void setVisible(Activity activity, boolean visible) {
        View root = activity.findViewById(android.R.id.content);
        if (root == null) return;
        update(root, visible);
        // Bản demo cũ dùng LinearLayout thay vì BottomNavigationView.
        // Tìm bằng tên ID tại runtime để không phụ thuộc vào resource do Owner Core quản lý.
        int demoCartId = activity.getResources().getIdentifier("btn_open_cart", "id", activity.getPackageName());
        if (demoCartId != 0) {
            View btn = activity.findViewById(demoCartId);
            if (btn != null && btn.getParent() instanceof View) {
                ((View) btn.getParent()).setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }
    }
    private static void update(View v, boolean visible) {
        if (v instanceof BottomNavigationView) {
            v.setVisibility(visible ? View.VISIBLE : View.GONE);
            return;
        }
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            for (int i = 0; i < group.getChildCount(); i++) update(group.getChildAt(i), visible);
        }
    }
}
