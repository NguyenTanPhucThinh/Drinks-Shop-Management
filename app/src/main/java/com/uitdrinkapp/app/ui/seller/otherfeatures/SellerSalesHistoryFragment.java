package com.uitdrinkapp.app.ui.seller.otherfeatures;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.button.MaterialButton;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.Order;
import com.uitdrinkapp.app.data.model.OrderStatus;
import com.uitdrinkapp.app.data.model.Review;
import com.uitdrinkapp.app.data.order.OrderRepository;
import com.uitdrinkapp.app.data.order.OrderRepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentSellerSalesHistoryBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Seller's read-only history of completed and cancelled shop orders. */
public final class SellerSalesHistoryFragment extends Fragment {
    private enum Filter { ALL, COMPLETED, CANCELLED, REJECTED }
    private static final Locale VIETNAMESE = Locale.forLanguageTag("vi-VN");

    private FragmentSellerSalesHistoryBinding binding;
    private OrderRepository repository;
    private SalesHistoryAdapter adapter;
    private Filter selectedFilter = Filter.ALL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSellerSalesHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repository = OrderRepositoryProvider.get(requireContext());
        adapter = new SalesHistoryAdapter(orderId -> openOrderDetails(orderId));
        binding.rvSellerHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSellerHistory.setAdapter(adapter);
        binding.btnSellerHistoryBack.setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.btnSellerHistoryAll.setOnClickListener(v -> select(Filter.ALL));
        binding.btnSellerHistoryCompleted.setOnClickListener(v -> select(Filter.COMPLETED));
        binding.btnSellerHistoryCancelled.setOnClickListener(v -> select(Filter.CANCELLED));
        binding.btnSellerHistoryRejected.setOnClickListener(v -> select(Filter.REJECTED));
        binding.btnSellerHistoryClearSearch.setOnClickListener(v ->
                binding.etSellerHistorySearch.setText(""));
        binding.etSellerHistorySearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                render();
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        render();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && repository != null) render();
    }

    private void select(Filter filter) {
        selectedFilter = filter;
        render();
    }

    private void render() {
        if (binding == null || repository == null || adapter == null) return;
        List<Order> terminalOrders = new ArrayList<>();
        int completedCount = 0;
        int cancelledCount = 0;
        int rejectedCount = 0;
        for (Order order : repository.getOrders()) {
            if (order.status == OrderStatus.COMPLETED) {
                completedCount++;
                terminalOrders.add(order);
            } else if (order.status == OrderStatus.CANCELLED) {
                cancelledCount++;
                terminalOrders.add(order);
            } else if (order.status == OrderStatus.REJECTED) {
                rejectedCount++;
                terminalOrders.add(order);
            }
        }

        int totalCount = terminalOrders.size();
        binding.btnSellerHistoryAll.setText(getString(R.string.seller_history_filter_all, totalCount));
        binding.btnSellerHistoryCompleted.setText(getString(
                R.string.seller_history_filter_completed, completedCount));
        binding.btnSellerHistoryCancelled.setText(getString(
                R.string.seller_history_filter_cancelled, cancelledCount));
        binding.btnSellerHistoryRejected.setText(getString(
                R.string.seller_history_filter_rejected, rejectedCount));
        styleFilter(binding.btnSellerHistoryAll, selectedFilter == Filter.ALL);
        styleFilter(binding.btnSellerHistoryCompleted, selectedFilter == Filter.COMPLETED);
        styleFilter(binding.btnSellerHistoryCancelled, selectedFilter == Filter.CANCELLED);
        styleFilter(binding.btnSellerHistoryRejected, selectedFilter == Filter.REJECTED);

        String query = binding.etSellerHistorySearch.getText() == null ? ""
                : binding.etSellerHistorySearch.getText().toString().trim().toLowerCase(VIETNAMESE);
        List<Order> visibleOrders = new ArrayList<>();
        for (Order order : terminalOrders) {
            if (!matchesFilter(order)) continue;
            if (!query.isEmpty() && !order.orderId.toLowerCase(VIETNAMESE).contains(query)) continue;
            visibleOrders.add(order);
        }
        Collections.sort(visibleOrders, Comparator.comparingLong((Order order) -> order.createdAt).reversed());

        List<SalesHistoryAdapter.Row> rows = new ArrayList<>();
        int index = 0;
        while (index < visibleOrders.size()) {
            Order first = visibleOrders.get(index);
            Calendar month = Calendar.getInstance(VIETNAMESE);
            month.setTimeInMillis(first.createdAt);
            int year = month.get(Calendar.YEAR);
            int monthValue = month.get(Calendar.MONTH);
            int end = index + 1;
            while (end < visibleOrders.size()) {
                Calendar nextMonth = Calendar.getInstance(VIETNAMESE);
                nextMonth.setTimeInMillis(visibleOrders.get(end).createdAt);
                if (nextMonth.get(Calendar.YEAR) != year
                        || nextMonth.get(Calendar.MONTH) != monthValue) break;
                end++;
            }
            rows.add(SalesHistoryAdapter.Row.month(
                    getString(R.string.seller_history_month, monthValue + 1, year), end - index));
            for (int i = index; i < end; i++) {
                Order order = visibleOrders.get(i);
                rows.add(SalesHistoryAdapter.Row.order(order, repository.getReview(order.orderId)));
            }
            index = end;
        }
        adapter.submit(rows);

        boolean empty = visibleOrders.isEmpty();
        binding.tvSellerHistoryEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.tvSellerHistoryEmpty.setText(query.isEmpty()
                ? R.string.seller_history_empty : R.string.seller_history_no_search_results);
        binding.btnSellerHistoryClearSearch.setVisibility(empty && !query.isEmpty()
                ? View.VISIBLE : View.GONE);
        binding.rvSellerHistory.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private boolean matchesFilter(Order order) {
        switch (selectedFilter) {
            case COMPLETED: return order.status == OrderStatus.COMPLETED;
            case CANCELLED: return order.status == OrderStatus.CANCELLED;
            case REJECTED: return order.status == OrderStatus.REJECTED;
            case ALL:
            default: return true;
        }
    }

    private void styleFilter(MaterialButton button, boolean active) {
        int background = active ? R.color.thien_seller_orders_accent
                : R.color.thien_seller_orders_card;
        int foreground = active ? R.color.thien_seller_orders_on_accent
                : R.color.thien_seller_orders_text;
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                requireContext().getColor(background)));
        button.setTextColor(requireContext().getColor(foreground));
        button.setStrokeColor(android.content.res.ColorStateList.valueOf(
                requireContext().getColor(active ? R.color.thien_seller_orders_accent
                        : R.color.thien_seller_orders_border)));
    }

    private void openOrderDetails(String orderId) {
        Bundle args = new Bundle();
        args.putString("order_id", orderId);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_sellerHistory_to_orderDetail, args);
    }

    @Override
    public void onDestroyView() {
        adapter = null;
        binding = null;
        super.onDestroyView();
    }
}
