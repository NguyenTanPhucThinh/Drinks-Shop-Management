package com.uitdrinkapp.app.ui.customer.order;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.ui.common.OrderSystemNotifications;
import com.uitdrinkapp.app.databinding.FragmentThienOrderTrackingBinding;
import com.uitdrinkapp.app.data.order.BuyerOrderRepository;
import com.uitdrinkapp.app.data.order.BuyerOrderRepositoryProvider;
import com.uitdrinkapp.app.data.model.Order;
import com.uitdrinkapp.app.data.model.OrderItem;
import com.uitdrinkapp.app.data.model.OrderStatus;

/** Buyer order tracking: read order status and allow only legitimate buyer actions. */
public final class OrderTrackingFragment extends Fragment {
    private FragmentThienOrderTrackingBinding binding;
    private BuyerOrderRepository repo;
    private String orderId;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                    @Nullable ViewGroup container,
                                                    @Nullable Bundle savedInstanceState) {
        binding = FragmentThienOrderTrackingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repo = BuyerOrderRepositoryProvider.get(requireContext());
        BuyerNavBars.setVisible(requireActivity(), false);
        orderId = getArguments() == null ? null : getArguments().getString("order_id");
        binding.btnThienTrackingBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnThienOpenHistory.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_thien_tracking_to_history));
        binding.btnThienCancelOrder.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle(R.string.thien_cancel_confirm_title)
                .setMessage(R.string.thien_cancel_confirm_body)
                .setNegativeButton(R.string.thien_close, null)
                .setPositiveButton(R.string.thien_cancel_order, (dialog, which) -> {
                    if (!repo.cancelOrder(orderId)) {
                        Toast.makeText(requireContext(), R.string.thien_cannot_cancel, Toast.LENGTH_SHORT).show();
                    } else {
                        OrderSystemNotifications.buyerCancelled(requireContext(), orderId);
                    }
                    render();
                }).show());
        binding.btnThienTrackingDetail.setOnClickListener(v -> openOrderDetails());
        binding.btnThienTrackingReview.setOnClickListener(v -> {
            Bundle args = new Bundle(); args.putString("order_id", orderId);
            NavHostFragment.findNavController(this).navigate(R.id.action_thien_tracking_to_review, args);
        });
        render();
    }
    @Override public void onResume() {
        super.onResume(); BuyerNavBars.setVisible(requireActivity(), false);
        if (repo != null && binding != null) render();
    }
    private void render() {
        Order order = orderId == null ? null : repo.getOrder(orderId);
        boolean found = order != null;
        binding.thienTrackingContent.setVisibility(found ? View.VISIBLE : View.GONE);
        binding.tvThienTrackingNotFound.setVisibility(found ? View.GONE : View.VISIBLE);
        if (!found) return;
        binding.tvThienTrackingId.setText(getString(R.string.thien_order_label, order.orderId));
        binding.tvThienTrackingTime.setText(OrderUiFormat.date(order.createdAt));
        binding.tvThienTrackingStatus.setText(OrderUiFormat.status(requireContext(), order.status));
        int bg, fg;
        switch (order.status) {
            case PENDING: bg = R.drawable.bg_thien_warning_pill; fg = R.color.thien_status_wait_text; break;
            case REJECTED:
            case CANCELLED: bg = R.drawable.bg_thien_status_cancel; fg = R.color.thien_status_cancel_text; break;
            default: bg = R.drawable.bg_thien_status_success; fg = R.color.thien_status_success_text; break;
        }
        binding.tvThienTrackingStatus.setBackgroundResource(bg);
        binding.tvThienTrackingStatus.setTextColor(requireContext().getColor(fg));
        binding.tvThienTrackingTotal.setText(OrderUiFormat.money(order.totalAmount()));
        boolean waiting=order.status==OrderStatus.PENDING;
        boolean finished=order.status==OrderStatus.COMPLETED;
        boolean cancelled=order.status==OrderStatus.CANCELLED || order.status==OrderStatus.REJECTED;
        binding.tvThienStepReceived.setText(cancelled ? "●  Đơn đã hủy" :
                (waiting ? "○  Đang chờ xử lý" : "✓  Đã nhận đơn"));
        binding.tvThienStepPreparing.setText(finished || order.status == OrderStatus.READY
                ? "✓  Đã pha chế xong" : (waiting || cancelled ? "○  Đang pha chế" : "●  Đang pha chế"));
        binding.tvThienStepDone.setText(finished ? "✓  Hoàn thành" : "○  Hoàn thành");
        binding.tvThienStepDone.setTextColor(requireContext().getColor(finished?R.color.primary:R.color.text_secondary));
        binding.thienTrackingItems.removeAllViews();
        for (OrderItem item : order.items) {
            TextView label = new TextView(requireContext());
            label.setText(getString(R.string.thien_checkout_item_line,
                    item.quantity, item.productName, item.size, OrderUiFormat.money(item.totalPrice())));
            label.setTextColor(getResources().getColor(R.color.text_primary, requireContext().getTheme()));
            label.setPadding(0, getResources().getDimensionPixelSize(R.dimen.spacing_8),
                    0, getResources().getDimensionPixelSize(R.dimen.spacing_8));
            binding.thienTrackingItems.addView(label);
        }
        binding.btnThienCancelOrder.setVisibility(order.status == OrderStatus.PENDING ? View.VISIBLE : View.GONE);

        binding.btnThienTrackingDetail.setVisibility(View.VISIBLE);
        binding.btnThienOpenHistory.setVisibility(View.GONE);
        binding.btnThienTrackingReview.setVisibility(finished ? View.VISIBLE : View.GONE);
        binding.btnThienTrackingReview.setText(repo.hasReview(order.orderId)
                ? R.string.thien_review_view : R.string.thien_review_action);
    }
    private void openOrderDetails() {
        if (orderId == null || repo.getOrder(orderId) == null) return;
        androidx.navigation.NavController nav = NavHostFragment.findNavController(this);
        androidx.navigation.NavBackStackEntry previous = nav.getPreviousBackStackEntry();
        if (previous != null && previous.getDestination().getId() == R.id.thien_order_detail
                && previous.getArguments() != null
                && orderId.equals(previous.getArguments().getString("order_id"))) {
            nav.popBackStack();
            return;
        }
        Bundle args = new Bundle();
        args.putString("order_id", orderId);
        nav.navigate(R.id.action_thien_tracking_to_detail, args);
    }
    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
