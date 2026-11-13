package com.uitdrinkapp.app.ui.customer.history;

import android.os.Bundle;
import android.content.res.ColorStateList;

import com.uitdrinkapp.app.ui.customer.order.BuyerNavBars;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.databinding.FragmentThienPurchaseHistoryBinding;
import com.uitdrinkapp.app.databinding.ItemThienHistoryBinding;
import com.google.android.material.button.MaterialButton;
import com.uitdrinkapp.app.ui.common.ProductThumbnail;
import com.uitdrinkapp.app.ui.customer.order.OrderUiFormat;
import com.uitdrinkapp.app.data.order.BuyerOrderRepository;
import com.uitdrinkapp.app.data.order.BuyerOrderRepositoryProvider;
import com.uitdrinkapp.app.data.model.Order;
import com.uitdrinkapp.app.data.model.OrderItem;
import com.uitdrinkapp.app.data.model.OrderStatus;

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
        binding.btnThienHistoryBack.setOnClickListener(v -> navigateToHome());
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navigateToHome();
                    }
                });
        binding.btnThienHistoryAll.setOnClickListener(v -> { selected = Filter.ALL; render(); });
        binding.btnThienHistoryCompleted.setOnClickListener(v -> { selected = Filter.COMPLETED; render(); });
        binding.btnThienHistoryCancelled.setOnClickListener(v -> { selected = Filter.CANCELLED; render(); });
        render();
    }
    private void navigateToHome() {
        NavController nav = NavHostFragment.findNavController(this);
        if (!nav.popBackStack(R.id.homeFragment, false)) {
            nav.navigate(R.id.homeFragment);
        }
    }
    @Override public void onResume() {
        super.onResume(); BuyerNavBars.setVisible(requireActivity(), true);
        if (binding != null && repo != null) render();
    }
    private void render() {
        styleFilter(binding.btnThienHistoryAll, selected == Filter.ALL);
        styleFilter(binding.btnThienHistoryCompleted, selected == Filter.COMPLETED);
        styleFilter(binding.btnThienHistoryCancelled, selected == Filter.CANCELLED);
        binding.thienHistoryContainer.removeAllViews();
        int visibleCount = 0;
        for (Order order : repo.getOrders()) {
            if ((selected == Filter.COMPLETED && order.status != OrderStatus.COMPLETED)
                    || (selected == Filter.CANCELLED && order.status != OrderStatus.CANCELLED
                            && order.status != OrderStatus.REJECTED)) continue;
            visibleCount++;
            ItemThienHistoryBinding row = ItemThienHistoryBinding.inflate(getLayoutInflater(), binding.thienHistoryContainer, false);
            row.tvThienHistoryId.setText(getString(R.string.thien_order_label, order.orderId));
            row.tvThienHistoryTime.setText(OrderUiFormat.date(order.createdAt));
            row.tvThienHistoryStatus.setText(OrderUiFormat.status(requireContext(), order.status));
            int bg, fg;
            switch (order.status) {
                case PENDING: bg = R.drawable.bg_thien_warning_pill; fg = R.color.thien_status_wait_text; break;
                case REJECTED:
                case CANCELLED: bg = R.drawable.bg_thien_status_cancel; fg = R.color.thien_status_cancel_text; break;
                default: bg = R.drawable.bg_thien_status_success; fg = R.color.thien_status_success_text; break;
            }
            row.tvThienHistoryStatus.setBackgroundResource(bg);
            row.tvThienHistoryStatus.setTextColor(requireContext().getColor(fg));
            row.tvThienHistoryTotal.setText(OrderUiFormat.money(order.totalAmount()));
            int qty = 0;
            for (OrderItem line : order.items) qty += line.quantity;
            String name = order.items.isEmpty() ? "Đồ uống" : order.items.get(0).productName;
            row.tvThienHistoryProducts.setText(qty + " món · " + name);
            ProductThumbnail.bind(row.ivThienHistoryImage,
                    order.items.isEmpty() ? null : order.items.get(0).productId);
            row.btnThienHistoryDetails.setOnClickListener(v -> {
                Bundle args = new Bundle(); args.putString("order_id", order.orderId);
                NavHostFragment.findNavController(this).navigate(R.id.action_thien_history_to_tracking, args);
            });
            row.thienHistoryRow.setOnClickListener(v -> row.btnThienHistoryDetails.performClick());
            boolean canReview = order.status == OrderStatus.COMPLETED;
            row.btnThienHistoryReview.setVisibility(canReview ? View.VISIBLE : View.GONE);
            row.btnThienHistoryReview.setText(repo.hasReview(order.orderId)
                    ? R.string.thien_review_view : R.string.thien_review_action);
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
