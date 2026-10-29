package com.uitdrinkapp.app.ui.customer.history;

import android.os.Bundle;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.databinding.FragmentThienPurchaseHistoryBinding;
import com.uitdrinkapp.app.databinding.ItemThienHistoryBinding;
import com.google.android.material.button.MaterialButton;
import com.uitdrinkapp.app.ui.customer.order.OrderUiFormat;
import com.uitdrinkapp.app.ui.customer.order.data.BuyerOrderRepository;
import com.uitdrinkapp.app.ui.customer.order.data.BuyerOrderRepositoryProvider;
import com.uitdrinkapp.app.ui.customer.order.data.DemoOrder;

/** Màn 10: lọc Tất cả / Hoàn thành / Đã hủy. */
public final class PurchaseHistoryFragment extends Fragment {
    private enum Filter { ALL, COMPLETED, CANCELLED }
    private Filter selected = Filter.ALL;
    private FragmentThienPurchaseHistoryBinding binding;
    private BuyerOrderRepository repo;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                    @Nullable ViewGroup container,
                                                    @Nullable Bundle savedInstanceState) {
        binding = FragmentThienPurchaseHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repo = BuyerOrderRepositoryProvider.get(requireContext());
        binding.btnThienHistoryAll.setOnClickListener(v -> { selected = Filter.ALL; render(); });
        binding.btnThienHistoryCompleted.setOnClickListener(v -> { selected = Filter.COMPLETED; render(); });
        binding.btnThienHistoryCancelled.setOnClickListener(v -> { selected = Filter.CANCELLED; render(); });
        render();
    }
    @Override public void onResume() { super.onResume(); if (binding != null && repo != null) render(); }
    private void render() {
        styleFilter(binding.btnThienHistoryAll, selected == Filter.ALL);
        styleFilter(binding.btnThienHistoryCompleted, selected == Filter.COMPLETED);
        styleFilter(binding.btnThienHistoryCancelled, selected == Filter.CANCELLED);
        binding.thienHistoryContainer.removeAllViews();
        int visibleCount = 0;
        for (DemoOrder order : repo.getOrders()) {
            if ((selected == Filter.COMPLETED && order.status != DemoOrder.Status.COMPLETED)
                    || (selected == Filter.CANCELLED && order.status != DemoOrder.Status.CANCELLED)) continue;
            visibleCount++;
            ItemThienHistoryBinding row = ItemThienHistoryBinding.inflate(getLayoutInflater(), binding.thienHistoryContainer, false);
            row.tvThienHistoryId.setText(getString(R.string.thien_order_label, order.orderId));
            row.tvThienHistoryTime.setText(OrderUiFormat.date(order.createdAt));
            row.tvThienHistoryStatus.setText(OrderUiFormat.status(requireContext(), order.status));
            row.tvThienHistoryStatus.setTextColor(getResources().getColor(
                    order.status == DemoOrder.Status.CANCELLED ? R.color.error : R.color.primary,
                    requireContext().getTheme()));
            row.tvThienHistoryTotal.setText(OrderUiFormat.money(order.totalAmount()));
            row.tvThienHistoryProducts.setText(getString(R.string.thien_products_count, order.items.size()));
            row.btnThienHistoryDetails.setOnClickListener(v -> {
                Bundle args = new Bundle(); args.putString("order_id", order.orderId);
                NavHostFragment.findNavController(this).navigate(R.id.action_thien_history_to_tracking, args);
            });
            boolean canReview = order.status == DemoOrder.Status.COMPLETED && !repo.hasReview(order.orderId);
            row.btnThienHistoryReview.setVisibility(canReview ? View.VISIBLE : View.GONE);
            row.btnThienHistoryReview.setOnClickListener(v -> {
                Bundle args = new Bundle(); args.putString("order_id", order.orderId);
                NavHostFragment.findNavController(this).navigate(R.id.action_thien_history_to_review, args);
            });
            binding.thienHistoryContainer.addView(row.getRoot());
        }
        binding.tvThienHistoryEmpty.setVisibility(visibleCount == 0 ? View.VISIBLE : View.GONE);
    }
    private void styleFilter(MaterialButton button, boolean active) {
        int bg = getResources().getColor(active ? R.color.primary : R.color.primary_light, requireContext().getTheme());
        int fg = getResources().getColor(active ? R.color.on_primary : R.color.primary, requireContext().getTheme());
        button.setBackgroundTintList(ColorStateList.valueOf(bg));
        button.setTextColor(fg);
    }
    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
