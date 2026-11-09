package com.uitdrinkapp.app.ui.seller.orders;

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
import com.uitdrinkapp.app.databinding.ItemThienSellerOrderBinding;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Renders Seller screen 15 cards from shared Order snapshots; no Seller-only storage. */
public final class SellerOrderAdapter extends RecyclerView.Adapter<SellerOrderAdapter.Holder> {
    public interface TransitionListener { void onTransition(String orderId, OrderStatus next); }
    public interface QuickViewListener { void onQuickView(String orderId); }
    private static final Locale VI = Locale.forLanguageTag("vi-VN");

    private final List<Order> items = new ArrayList<>();
    private final TransitionListener transitions;
    private final QuickViewListener quickView;
    private final boolean showActions;

    public SellerOrderAdapter(TransitionListener transitions, QuickViewListener quickView) {
        this(transitions, quickView, true);
    }

    public SellerOrderAdapter(TransitionListener transitions, QuickViewListener quickView,
            boolean showActions) {
        this.transitions = transitions;
        this.quickView = quickView;
        this.showActions = showActions;
    }

    public void submit(List<Order> value) {
        items.clear();
        if (value != null) items.addAll(value);
        notifyDataSetChanged();
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemThienSellerOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder h, int position) {
        Order order = items.get(position);
        ItemThienSellerOrderBinding b = h.binding;
        b.tvThienSellerOrderCode.setText(b.getRoot().getContext().getString(
                R.string.thien_seller_orders_code, order.orderId));
        b.tvThienSellerOrderTime.setText(new SimpleDateFormat("dd/MM · HH:mm", VI)
                .format(new Date(order.createdAt)));
        b.tvThienSellerOrderStatus.setText(statusLabel(order.status));
        int bg, fg;
        switch (order.status) {
            case COMPLETED:
                bg = R.color.thien_seller_orders_status_success_bg;
                fg = R.color.thien_seller_orders_status_success_text;
                break;
            case CANCELLED:
            case REJECTED:
                bg = R.color.thien_seller_orders_status_cancel_bg;
                fg = R.color.thien_seller_orders_status_cancel_text;
                break;
            case PENDING:
                bg = R.color.thien_seller_orders_status_new_bg;
                fg = R.color.thien_seller_orders_status_new_text;
                break;
            default:
                bg = R.color.thien_seller_orders_status_preparing_bg;
                fg = R.color.thien_seller_orders_status_preparing_text;
                break;
        }
        b.tvThienSellerOrderStatus.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(b.getRoot().getContext(), bg)));
        b.tvThienSellerOrderStatus.setTextColor(
                ContextCompat.getColor(b.getRoot().getContext(), fg));

        StringBuilder description = new StringBuilder();
        for (OrderItem item : order.items) {
            if (description.length() > 0) description.append("\n");
            description.append(item.quantity).append(" × ").append(item.productName)
                    .append(" (").append(item.size).append(")");
            if (!item.toppings.isEmpty()) description.append(" · ").append(item.toppingLabel());
        }
        b.tvThienSellerOrderProducts.setText(description.length() == 0
                ? b.getRoot().getContext().getString(R.string.thien_seller_orders_no_products)
                : description.toString());
        b.tvThienSellerOrderTable.setText(order.tableNumber == null || order.tableNumber.isEmpty()
                ? b.getRoot().getContext().getString(R.string.thien_seller_orders_no_table)
                : b.getRoot().getContext().getString(R.string.thien_seller_orders_table, order.tableNumber));
        b.tvThienSellerOrderTotal.setText(b.getRoot().getContext().getString(
                R.string.thien_seller_orders_money,
                NumberFormat.getIntegerInstance(VI).format(order.totalAmount())));
        b.btnThienSellerOrderQuickView.setOnClickListener(v -> quickView.onQuickView(order.orderId));
        b.getRoot().setOnClickListener(v -> quickView.onQuickView(order.orderId));

        OrderStatus primary = SellerOrderUiPolicy.primaryNext(order.status);
        OrderStatus secondary = SellerOrderUiPolicy.secondaryNext(order.status);
        b.llThienSellerOrderActions.setVisibility(!showActions || primary == null && secondary == null
                ? View.GONE : View.VISIBLE);
        b.btnThienSellerOrderPrimary.setVisibility(primary == null ? View.GONE : View.VISIBLE);
        b.btnThienSellerOrderSecondary.setVisibility(secondary == null ? View.GONE : View.VISIBLE);
        if (primary != null) {
            b.btnThienSellerOrderPrimary.setText(actionLabel(order.status, primary));
            b.btnThienSellerOrderPrimary.setOnClickListener(v ->
                    transitions.onTransition(order.orderId, primary));
        } else b.btnThienSellerOrderPrimary.setOnClickListener(null);
        if (secondary != null) {
            b.btnThienSellerOrderSecondary.setText(actionLabel(order.status, secondary));
            b.btnThienSellerOrderSecondary.setOnClickListener(v ->
                    transitions.onTransition(order.orderId, secondary));
        } else b.btnThienSellerOrderSecondary.setOnClickListener(null);
    }

    private int actionLabel(OrderStatus current, OrderStatus target) {
        if (target == OrderStatus.CANCELLED) return current == OrderStatus.PENDING
                ? R.string.thien_seller_orders_reject : R.string.thien_seller_orders_cancel;
        switch (target) {
            case DELIVERING: return R.string.thien_seller_orders_accept;
            case PREPARING: return R.string.thien_seller_orders_start_preparing;
            case READY: return R.string.thien_seller_orders_mark_ready;
            case COMPLETED: return R.string.thien_seller_orders_finish;
            default: return R.string.thien_seller_orders_accept;
        }
    }

    private int statusLabel(OrderStatus status) {
        if (status == null) return R.string.thien_seller_orders_status_new;
        switch (status) {
            case PENDING: return R.string.thien_seller_orders_status_new;
            case ACCEPTED: return R.string.thien_seller_orders_status_accepted;
            case PREPARING: return R.string.thien_seller_orders_status_preparing;
            case READY: return R.string.thien_seller_orders_status_ready;
            case DELIVERING: return R.string.thien_seller_orders_status_preparing;
            case COMPLETED: return R.string.thien_seller_orders_status_completed;
            case CANCELLED: return R.string.thien_seller_orders_status_cancelled;
            case REJECTED: return R.string.thien_seller_orders_status_rejected;
            default: return R.string.thien_seller_orders_status_new;
        }
    }

    @Override public int getItemCount() { return items.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemThienSellerOrderBinding binding;
        Holder(ItemThienSellerOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
