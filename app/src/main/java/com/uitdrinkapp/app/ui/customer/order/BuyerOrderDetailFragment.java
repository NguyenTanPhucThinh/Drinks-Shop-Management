package com.uitdrinkapp.app.ui.customer.order;

import android.os.Bundle;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.button.MaterialButton;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.FulfillmentMethod;
import com.uitdrinkapp.app.data.model.Order;
import com.uitdrinkapp.app.data.model.OrderItem;
import com.uitdrinkapp.app.data.model.OrderStatus;
import com.uitdrinkapp.app.data.model.Temperature;
import com.uitdrinkapp.app.data.order.BuyerOrderRepository;
import com.uitdrinkapp.app.data.order.BuyerOrderRepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentThienBuyerOrderDetailBinding;

/** Read-only order snapshot shared by history and tracking. */
public final class BuyerOrderDetailFragment extends Fragment {
    private FragmentThienBuyerOrderDetailBinding binding;
    private BuyerOrderRepository repo;
    private String orderId;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentThienBuyerOrderDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        repo = BuyerOrderRepositoryProvider.get(requireContext());
        orderId = getArguments() == null ? null : getArguments().getString("order_id");
        binding.btnGd07DetailBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        render();
    }

    @Override public void onResume() {
        super.onResume();
        if (binding != null && repo != null) render();
    }

    private void render() {
        LinearLayout root = binding.llGd07DetailCards;
        root.removeAllViews();
        Order order = orderId == null ? null : repo.getOrder(orderId);
        if (order == null) {
            card(root).addView(text(getString(R.string.thien_order_not_found), 14, R.color.text_secondary, false));
            return;
        }
        LinearLayout summary = card(root);
        summary.addView(text(getString(R.string.gd07_order_id), 12, R.color.text_secondary, false));
        summary.addView(text("#" + order.orderId, 16, R.color.text_primary, true));
        pair(summary, getString(R.string.gd07_fulfillment), fulfillmentSummary(order), false, R.color.text_primary);

        LinearLayout items = card(root);
        int quantity = 0;
        for (OrderItem item : order.items) quantity += item.quantity;
        items.addView(text(getString(R.string.gd07_items, quantity), 16, R.color.text_primary, true));
        if (order.items.isEmpty()) items.addView(text(getString(R.string.task03_buyer_no_items), 14, R.color.text_secondary, false));
        for (OrderItem item : order.items) {
            pair(items, getString(R.string.gd07_item_name, item.quantity, item.productName),
                    OrderUiFormat.money(item.totalPrice()), true, R.color.text_primary);
            divider(items);
            pair(items, getString(R.string.gd07_size), getString(R.string.gd07_size_value, item.size), false, R.color.text_primary);
            pair(items, getString(R.string.gd07_sugar), percent(item.sugarPercent), false, R.color.text_primary);
            pair(items, getString(R.string.gd07_ice), item.temperature == Temperature.HOT
                    ? getString(R.string.gd07_not_applicable) : item.temperature == null
                    ? getString(R.string.task03_buyer_unknown) : percent(item.icePercent), false, R.color.text_primary);
            pair(items, getString(R.string.gd07_temperature), temperature(item.temperature), false, R.color.text_primary);
            pair(items, getString(R.string.gd07_fulfillment), fulfillment(item.fulfillmentMethod), false, R.color.text_primary);
            pair(items, getString(R.string.gd07_topping), item.toppings.isEmpty()
                    ? getString(R.string.task03_buyer_no_toppings) : item.toppingLabel(), false, R.color.text_primary);
        }
        LinearLayout notes = card(root);
        notes.addView(text(getString(R.string.gd07_note), 15, R.color.text_primary, true));
        notes.addView(text(order.note == null || order.note.trim().isEmpty()
                ? getString(R.string.task03_buyer_no_note) : order.note, 14, R.color.text_primary, false));

        LinearLayout payment = card(root);
        payment.addView(text(getString(R.string.gd07_payment), 16, R.color.text_primary, true));
        pair(payment, getString(R.string.gd07_subtotal), OrderUiFormat.money(order.subtotal), false, R.color.text_primary);
        pair(payment, getString(R.string.gd07_discount), (order.discount > 0 ? "−" : "")
                + OrderUiFormat.money(order.discount), false, R.color.primary);
        pair(payment, getString(R.string.gd07_voucher), order.voucherCode == null || order.voucherCode.trim().isEmpty()
                ? getString(R.string.task03_buyer_no_toppings) : order.voucherCode, false, R.color.text_primary);
        divider(payment);
        pair(payment, getString(R.string.gd07_total), OrderUiFormat.money(order.totalAmount()), true, R.color.primary);
        if (isActive(order.status)) {
            MaterialButton track = new MaterialButton(requireContext());
            track.setId(R.id.btn_gd07_track_order);
            track.setText(R.string.gd07_track);
            track.setAllCaps(false);
            track.setMinHeight(dp(48));
            root.addView(track, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            track.setOnClickListener(v -> openTracking());
        }
    }

    private void openTracking() {
        Order current = repo.getOrder(orderId);
        if (current == null || !isActive(current.status)) { render(); return; }
        NavController nav = NavHostFragment.findNavController(this);
        NavBackStackEntry previous = nav.getPreviousBackStackEntry();
        if (previous != null && previous.getDestination().getId() == R.id.thien_tracking
                && previous.getArguments() != null
                && orderId.equals(previous.getArguments().getString("order_id"))) {
            nav.popBackStack();
            return;
        }
        Bundle args = new Bundle(); args.putString("order_id", orderId);
        nav.navigate(R.id.action_thien_detail_to_tracking, args);
    }

    private static boolean isActive(OrderStatus status) {
        return status == OrderStatus.PENDING || status == OrderStatus.ACCEPTED
                || status == OrderStatus.PREPARING || status == OrderStatus.DELIVERING || status == OrderStatus.READY;
    }

    private String fulfillmentSummary(Order order) {
        boolean counter = false, takeaway = false, unknown = false;
        for (OrderItem item : order.items) {
            if (item.fulfillmentMethod == FulfillmentMethod.AT_COUNTER) counter = true;
            else if (item.fulfillmentMethod == FulfillmentMethod.TAKEAWAY) takeaway = true;
            else unknown = true;
        }
        if (counter && takeaway) return getString(R.string.gd07_mixed);
        if (unknown) return getString(counter || takeaway ? R.string.gd07_partial_unknown : R.string.task03_buyer_unknown);
        return fulfillment(counter ? FulfillmentMethod.AT_COUNTER : takeaway ? FulfillmentMethod.TAKEAWAY : null);
    }
    private String fulfillment(@Nullable FulfillmentMethod value) {
        return getString(value == FulfillmentMethod.AT_COUNTER ? R.string.task03_buyer_counter
                : value == FulfillmentMethod.TAKEAWAY ? R.string.task03_buyer_takeaway : R.string.task03_buyer_unknown);
    }
    private String temperature(@Nullable Temperature value) {
        return getString(value == Temperature.HOT ? R.string.task03_buyer_hot
                : value == Temperature.ICED ? R.string.task03_buyer_iced : R.string.task03_buyer_unknown);
    }
    private String percent(int value) { return getString(R.string.gd07_percent, value); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private LinearLayout card(LinearLayout parent) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_thien_card);
        int padding = dp(14); card.setPadding(padding, padding, padding, padding);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(12); parent.addView(card, lp);
        return card;
    }
    private TextView text(String value, int sp, int color, boolean bold) {
        TextView tv = new TextView(requireContext());
        tv.setText(value); tv.setTextSize(sp); tv.setTextColor(requireContext().getColor(color));
        if (bold) tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setPadding(0, dp(3), 0, dp(3));
        return tv;
    }
    private void pair(LinearLayout parent, String label, String value, boolean bold, int valueColor) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        TextView left = text(label, bold ? 15 : 13, bold ? R.color.text_primary : R.color.text_secondary, bold);
        TextView right = text(value, bold ? 15 : 13, valueColor, bold);
        right.setGravity(Gravity.END);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        leftParams.setMarginEnd(dp(8));
        row.addView(left, leftParams);
        row.addView(right, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        parent.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }
    private void divider(LinearLayout parent) {
        View line = new View(requireContext());
        line.setBackgroundColor(requireContext().getColor(R.color.thien_border));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        lp.topMargin = dp(4); lp.bottomMargin = dp(4); parent.addView(line, lp);
    }
    @Override public void onDestroyView() { binding = null; super.onDestroyView(); }
}
