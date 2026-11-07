package com.uitdrinkapp.app.ui.seller.orders;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.button.MaterialButton;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.Order;
import com.uitdrinkapp.app.data.model.OrderItem;
import com.uitdrinkapp.app.data.model.OrderStatus;
import com.uitdrinkapp.app.data.order.OrderRepository;
import com.uitdrinkapp.app.data.order.OrderRepositoryProvider;
import com.uitdrinkapp.app.data.order.OrderTransitions;
import com.uitdrinkapp.app.databinding.FragmentSellerOrdersBinding;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Task02 GD04 CORE. Seller order list/actions; full detail navigation is intentionally deferred. */
public final class OrderManagementFragment extends Fragment {
    private static final String STATE_FILTER = "thien_seller_filter";
    private static final Locale VI = Locale.forLanguageTag("vi-VN");

    private FragmentSellerOrdersBinding binding;
    private OrderRepository orders;
    private SellerOrderAdapter adapter;
    private SellerOrderUiPolicy.Filter filter = SellerOrderUiPolicy.Filter.ALL;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSellerOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        if (state != null) {
            try { filter = SellerOrderUiPolicy.Filter.valueOf(
                    state.getString(STATE_FILTER, SellerOrderUiPolicy.Filter.ALL.name())); }
            catch (IllegalArgumentException ignored) { filter = SellerOrderUiPolicy.Filter.ALL; }
        }
        orders = OrderRepositoryProvider.get(requireContext());
        adapter = new SellerOrderAdapter(this::confirmTransition, this::openQuickSummary);
        binding.rvThienSellerOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvThienSellerOrders.setAdapter(adapter);
        binding.btnThienSellerOrdersBack.setOnClickListener(clicked ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.btnThienSellerFilterAll.setOnClickListener(clicked -> select(SellerOrderUiPolicy.Filter.ALL));
        binding.btnThienSellerFilterNew.setOnClickListener(clicked -> select(SellerOrderUiPolicy.Filter.NEW));
        binding.btnThienSellerFilterPreparing.setOnClickListener(clicked -> select(SellerOrderUiPolicy.Filter.PREPARING));
        refresh();
    }

    private void select(SellerOrderUiPolicy.Filter next) {
        filter = next;
        refresh();
    }

    @Override public void onResume() {
        super.onResume();
        if (binding != null && orders != null && adapter != null) refresh();
    }

    private void refresh() {
        List<Order> all = new ArrayList<>(orders.getOrders());
        // Repository contract returns snapshots. Sort the copies, not the persistence data.
        Collections.sort(all, Comparator.comparingLong((Order item) -> item.createdAt)
                .reversed().thenComparing(item -> item.orderId));
        int newCount = 0;
        int preparingCount = 0;
        List<Order> visible = new ArrayList<>();
        for (Order item : all) {
            if (SellerOrderUiPolicy.matches(item.status, SellerOrderUiPolicy.Filter.NEW)) newCount++;
            if (SellerOrderUiPolicy.matches(item.status, SellerOrderUiPolicy.Filter.PREPARING)) preparingCount++;
            if (SellerOrderUiPolicy.matches(item.status, filter)) visible.add(item);
        }
        binding.btnThienSellerFilterAll.setText(getString(R.string.thien_seller_orders_filter_all, all.size()));
        binding.btnThienSellerFilterNew.setText(getString(R.string.thien_seller_orders_filter_new, newCount));
        binding.btnThienSellerFilterPreparing.setText(getString(
                R.string.thien_seller_orders_filter_preparing, preparingCount));
        paintFilter(binding.btnThienSellerFilterAll, filter == SellerOrderUiPolicy.Filter.ALL);
        paintFilter(binding.btnThienSellerFilterNew, filter == SellerOrderUiPolicy.Filter.NEW);
        paintFilter(binding.btnThienSellerFilterPreparing, filter == SellerOrderUiPolicy.Filter.PREPARING);
        adapter.submit(visible);
        binding.tvThienSellerOrdersEmpty.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void paintFilter(MaterialButton button, boolean selected) {
        int bg = ContextCompat.getColor(requireContext(), selected
                ? R.color.thien_seller_orders_accent : R.color.thien_seller_orders_card);
        int fg = ContextCompat.getColor(requireContext(), selected
                ? R.color.thien_seller_orders_on_accent : R.color.thien_seller_orders_text);
        button.setBackgroundTintList(ColorStateList.valueOf(bg));
        button.setTextColor(fg);
        button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                selected ? R.color.thien_seller_orders_accent : R.color.thien_seller_orders_border)));
    }

    private void confirmTransition(String id, OrderStatus target) {
        Order fresh = orders.getOrder(id);
        if (fresh == null || !OrderTransitions.canSellerTransition(fresh.status, target)) {
            Toast.makeText(requireContext(), R.string.thien_seller_orders_invalid, Toast.LENGTH_SHORT).show();
            refresh();
            return;
        }
        int label = target == OrderStatus.CANCELLED
                ? (fresh.status == OrderStatus.PENDING ? R.string.thien_seller_orders_reject
                        : R.string.thien_seller_orders_cancel)
                : labelForTarget(target);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.thien_seller_orders_confirm_title)
                .setMessage(getString(R.string.thien_seller_orders_confirm_body,
                        fresh.orderId, getString(label)))
                .setNegativeButton(R.string.thien_seller_orders_close, null)
                .setPositiveButton(R.string.thien_seller_orders_confirm_yes, (dialog, which) -> {
                    // Re-read and enforce transition again when confirmation is pressed.
                    Order now = orders.getOrder(id);
                    boolean valid = now != null && OrderTransitions.canSellerTransition(now.status, target);
                    boolean changed = valid && orders.updateStatus(id, target);
                    Toast.makeText(requireContext(), changed
                            ? R.string.thien_seller_orders_updated : R.string.thien_seller_orders_invalid,
                            Toast.LENGTH_SHORT).show();
                    refresh();
                }).show();
    }

    private int labelForTarget(OrderStatus next) {
        switch (next) {
            case DELIVERING: return R.string.thien_seller_orders_accept;
            case PREPARING: return R.string.thien_seller_orders_start_preparing;
            case READY: return R.string.thien_seller_orders_mark_ready;
            case COMPLETED: return R.string.thien_seller_orders_finish;
            default: return R.string.thien_seller_orders_confirm_yes;
        }
    }

    /** Standalone quick view; navigating to GD03 OrderDetailFragment is a later integration. */
    private void openQuickSummary(String id) {
        Order order = orders.getOrder(id);
        if (order == null) {
            Toast.makeText(requireContext(), R.string.thien_seller_orders_not_found, Toast.LENGTH_SHORT).show();
            refresh();
            return;
        }
        StringBuilder lines = new StringBuilder();
        for (OrderItem item : order.items) {
            if (lines.length() > 0) lines.append("\n");
            lines.append(item.quantity).append(" × ").append(item.productName)
                    .append(" · ").append(item.size);
        }
        if (lines.length() == 0) lines.append(getString(R.string.thien_seller_orders_no_products));
        String table = order.tableNumber == null || order.tableNumber.trim().isEmpty()
                ? getString(R.string.thien_seller_orders_no_table)
                : getString(R.string.thien_seller_orders_table, order.tableNumber);
        String total = NumberFormat.getIntegerInstance(VI).format(order.totalAmount());
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.thien_seller_orders_quick_title, order.orderId))
                .setMessage(getString(R.string.thien_seller_orders_quick_body, table, lines.toString(), total))
                .setPositiveButton(R.string.thien_seller_orders_close, null).show();
    }

    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        state.putString(STATE_FILTER, filter.name());
        super.onSaveInstanceState(state);
    }

    @Override public void onDestroyView() {
        binding.rvThienSellerOrders.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }
}
