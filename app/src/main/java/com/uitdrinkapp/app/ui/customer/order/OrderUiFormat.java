package com.uitdrinkapp.app.ui.customer.order;

import android.content.Context;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.ui.customer.order.data.DemoOrder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class OrderUiFormat {
    private static final Locale VI = new Locale("vi", "VN");
    private OrderUiFormat() { }
    public static String money(long value) {
        return NumberFormat.getCurrencyInstance(VI).format(value);
    }
    public static String date(long time) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", VI).format(new Date(time));
    }
    public static String status(Context context, DemoOrder.Status status) {
        switch (status) {
            case PENDING: return context.getString(R.string.thien_status_pending);
            case DELIVERING: return context.getString(R.string.thien_status_delivering);
            case COMPLETED: return context.getString(R.string.thien_status_completed);
            default: return context.getString(R.string.thien_status_cancelled);
        }
    }
}
