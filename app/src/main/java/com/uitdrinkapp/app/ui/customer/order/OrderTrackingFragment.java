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
import com.uitdrinkapp.app.databinding.FragmentThienOrderTrackingBinding;
import com.uitdrinkapp.app.ui.customer.order.data.BuyerOrderRepository;
import com.uitdrinkapp.app.ui.customer.order.data.BuyerOrderRepositoryProvider;
import com.uitdrinkapp.app.ui.customer.order.data.CartLine;
import com.uitdrinkapp.app.ui.customer.order.data.DemoOrder;

/** Màn 09: theo dõi, hủy đơn ở trạng thái PENDING; nút demo đổi COMPLETED. */
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
        orderId = getArguments() == null ? null : getArguments().getString("order_id");
        binding.btnThienTrackingBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnThienOpenHistory.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_thien_tracking_to_history));
        binding.btnThienCancelOrder.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle(R.string.thien_cancel_confirm_title)
                .setMessage(R.string.thien_cancel_confirm_body)
                .setNegativeButton(R.string.thien_close, null)
                .setPositiveButton(R.string.thien_cancel_order, (dialog, which) -> {
                    if (!repo.cancelOrder(orderId))
                        Toast.makeText(requireContext(), R.string.thien_cannot_cancel, Toast.LENGTH_SHORT).show();
                    render();
                }).show());
        binding.btnThienSimulateComplete.setOnClickListener(v -> {
            repo.simulateComplete(orderId); render();
        });
        render();
    }
    @Override public void onResume() { super.onResume(); if (repo != null && binding != null) render(); }
    private void render() {
        DemoOrder order = orderId == null ? null : repo.getOrder(orderId);
        boolean found = order != null;
        binding.thienTrackingContent.setVisibility(found ? View.VISIBLE : View.GONE);
        binding.tvThienTrackingNotFound.setVisibility(found ? View.GONE : View.VISIBLE);
        if (!found) return;
        binding.tvThienTrackingId.setText(getString(R.string.thien_order_label, order.orderId));
        binding.tvThienTrackingTime.setText(OrderUiFormat.date(order.createdAt));
        binding.tvThienTrackingStatus.setText(OrderUiFormat.status(requireContext(), order.status));
        binding.tvThienTrackingStatus.setTextColor(getResources().getColor(
                order.status == DemoOrder.Status.CANCELLED ? R.color.error : R.color.primary,
                requireContext().getTheme()));
        binding.tvThienTrackingTotal.setText(OrderUiFormat.money(order.totalAmount()));
        boolean waiting=order.status==DemoOrder.Status.PENDING;
        boolean finished=order.status==DemoOrder.Status.COMPLETED;
        boolean cancelled=order.status==DemoOrder.Status.CANCELLED;
        binding.tvThienStepReceived.setText(cancelled?"●  Đơn đã hủy":"●  Đã nhận đơn");
        binding.tvThienStepPreparing.setText(finished?"✓  Đã pha chế xong":(waiting?"○  Đang chờ pha chế":"●  Đang pha chế"));
        binding.tvThienStepDone.setText(finished?"✓  Hoàn thành":"○  Hoàn thành");
        binding.tvThienStepDone.setTextColor(requireContext().getColor(finished?R.color.primary:R.color.text_secondary));
        binding.thienTrackingItems.removeAllViews();
        for (CartLine item : order.items) {
            TextView label = new TextView(requireContext());
            label.setText(getString(R.string.thien_checkout_item_line,
                    item.quantity, item.productName, item.size.name(), OrderUiFormat.money(item.totalPrice())));
            label.setTextColor(getResources().getColor(R.color.text_primary, requireContext().getTheme()));
            label.setPadding(0, getResources().getDimensionPixelSize(R.dimen.spacing_8),
                    0, getResources().getDimensionPixelSize(R.dimen.spacing_8));
            binding.thienTrackingItems.addView(label);
        }
        binding.btnThienCancelOrder.setVisibility(order.status == DemoOrder.Status.PENDING ? View.VISIBLE : View.GONE);
        binding.btnThienSimulateComplete.setVisibility(order.status == DemoOrder.Status.PENDING
                || order.status == DemoOrder.Status.DELIVERING ? View.VISIBLE : View.GONE);
    }
    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
