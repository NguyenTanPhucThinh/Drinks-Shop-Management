package com.uitdrinkapp.app.ui.seller.otherfeatures;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.Order;
import com.uitdrinkapp.app.data.model.OrderItem;
import com.uitdrinkapp.app.data.model.OrderStatus;
import com.uitdrinkapp.app.data.model.Review;
import com.uitdrinkapp.app.databinding.ItemSellerHistoryMonthBinding;
import com.uitdrinkapp.app.databinding.ItemSellerHistoryOrderBinding;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

final class SalesHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    interface DetailListener { void onOpen(String orderId); }

    private static final int TYPE_MONTH = 0;
    private static final int TYPE_ORDER = 1;
    private static final Locale VIETNAMESE = Locale.forLanguageTag("vi-VN");
    private final List<Row> rows = new ArrayList<>();
    private final DetailListener detailListener;

    SalesHistoryAdapter(DetailListener detailListener) {
        this.detailListener = detailListener;
    }

    void submit(List<Row> value) {
        rows.clear();
        if (value != null) rows.addAll(value);
        notifyDataSetChanged();
    }

    @Override public int getItemViewType(int position) {
        return rows.get(position).order == null ? TYPE_MONTH : TYPE_ORDER;
    }

    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_MONTH) {
            return new MonthHolder(ItemSellerHistoryMonthBinding.inflate(inflater, parent, false));
        }
        return new OrderHolder(ItemSellerHistoryOrderBinding.inflate(inflater, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (holder instanceof MonthHolder) {
            MonthHolder monthHolder = (MonthHolder) holder;
            monthHolder.binding.tvSellerHistoryMonth.setText(row.monthTitle);
            monthHolder.binding.tvSellerHistoryMonthCount.setText(
                    monthHolder.binding.getRoot().getContext().getString(
                            R.string.seller_history_month_count, row.monthCount));
            return;
        }
        bindOrder(((OrderHolder) holder).binding, row.order, row.review);
    }

    private void bindOrder(ItemSellerHistoryOrderBinding binding, Order order, Review review) {
        android.content.Context context = binding.getRoot().getContext();
        binding.tvSellerHistoryOrderId.setText(context.getString(
                R.string.seller_history_order_code, order.orderId));
        binding.tvSellerHistoryDate.setText(formatDate(order.createdAt));
        binding.tvSellerHistoryCustomerTable.setText(context.getString(
                R.string.seller_history_customer_table, order.customerName,
                order.tableNumber == null || order.tableNumber.trim().isEmpty()
                        ? context.getString(R.string.seller_history_no_table)
                        : context.getString(R.string.seller_history_table, order.tableNumber)));
        binding.tvSellerHistoryStatus.setText(statusLabel(context, order.status));
        int statusBackground = order.status == OrderStatus.COMPLETED
                ? R.color.thien_seller_orders_status_preparing_bg
                : R.color.thien_seller_orders_status_cancel_bg;
        int statusForeground = order.status == OrderStatus.COMPLETED
                ? R.color.thien_seller_orders_status_preparing_text
                : R.color.thien_seller_orders_status_cancel_text;
        binding.tvSellerHistoryStatus.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(context, statusBackground)));
        binding.tvSellerHistoryStatus.setTextColor(ContextCompat.getColor(context, statusForeground));

        binding.tvSellerHistoryProducts.setText(productSummary(context, order.items));
        binding.tvSellerHistoryTotal.setText(context.getString(R.string.seller_history_money,
                NumberFormat.getIntegerInstance(VIETNAMESE).format(order.totalAmount())));
        if (review == null) {
            binding.tvSellerHistoryReview.setText(R.string.seller_history_not_reviewed);
            binding.tvSellerHistoryReview.setTextColor(
                    ContextCompat.getColor(context, R.color.thien_seller_orders_muted));
        } else {
            binding.tvSellerHistoryReview.setText(context.getString(
                    R.string.seller_history_reviewed, review.rating));
            binding.tvSellerHistoryReview.setTextColor(
                    ContextCompat.getColor(context, R.color.thien_seller_orders_accent));
        }
        binding.btnSellerHistoryDetails.setOnClickListener(v -> detailListener.onOpen(order.orderId));
        binding.getRoot().setOnClickListener(v -> detailListener.onOpen(order.orderId));
    }

    private String productSummary(android.content.Context context, List<OrderItem> items) {
        if (items == null || items.isEmpty()) return context.getString(R.string.seller_history_no_items);
        OrderItem first = items.get(0);
        String summary = context.getString(R.string.seller_history_item_summary,
                first.productName, first.quantity);
        if (items.size() > 1) {
            summary += context.getString(R.string.seller_history_more_items, items.size() - 1);
        }
        return summary;
    }

    private int statusLabel(android.content.Context context, OrderStatus status) {
        if (status == OrderStatus.COMPLETED) return R.string.seller_history_status_completed;
        if (status == OrderStatus.REJECTED) return R.string.seller_history_status_rejected;
        return R.string.seller_history_status_cancelled;
    }

    private String formatDate(long timestamp) {
        return new SimpleDateFormat("dd/MM/yyyy · HH:mm", VIETNAMESE).format(new Date(timestamp));
    }

    @Override public int getItemCount() { return rows.size(); }

    static final class Row {
        final String monthTitle;
        final int monthCount;
        final Order order;
        final Review review;
        private Row(String monthTitle, int monthCount, Order order, Review review) {
            this.monthTitle = monthTitle;
            this.monthCount = monthCount;
            this.order = order;
            this.review = review;
        }
        static Row month(String title, int count) { return new Row(title, count, null, null); }
        static Row order(Order order, Review review) { return new Row(null, 0, order, review); }
    }

    private static final class MonthHolder extends RecyclerView.ViewHolder {
        final ItemSellerHistoryMonthBinding binding;
        MonthHolder(ItemSellerHistoryMonthBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static final class OrderHolder extends RecyclerView.ViewHolder {
        final ItemSellerHistoryOrderBinding binding;
        OrderHolder(ItemSellerHistoryOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
