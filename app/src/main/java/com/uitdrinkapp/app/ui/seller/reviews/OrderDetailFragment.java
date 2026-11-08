package com.uitdrinkapp.app.ui.seller.reviews;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.Order;
import com.uitdrinkapp.app.data.model.OrderItem;
import com.uitdrinkapp.app.data.model.OrderStatus;
import com.uitdrinkapp.app.data.model.PaymentMethod;
import com.uitdrinkapp.app.data.model.Review;
import com.uitdrinkapp.app.data.order.OrderRepository;
import com.uitdrinkapp.app.data.order.OrderRepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentSellerOrderDetailBinding;
import com.uitdrinkapp.app.databinding.ItemSellerOrderDetailProductBinding;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Task02 GD03: Seller read-only order detail and review screen.
 * Open with argument "order_id" from Seller Order/Notification/History/Home.
 * Reads existing SharedPreferences-backed OrderRepository; never mutates orders/reviews.
 */
public final class OrderDetailFragment extends Fragment {
    public static final String ARG_ORDER_ID = "order_id";
    private static final Locale VIETNAMESE = Locale.forLanguageTag("vi-VN");
    private FragmentSellerOrderDetailBinding binding;
    private OrderRepository repository;
    private String orderId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSellerOrderDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repository = OrderRepositoryProvider.get(requireContext());
        orderId = getArguments() == null ? null : getArguments().getString(ARG_ORDER_ID);
        binding.btnSellerDetailBack.setOnClickListener(clicked ->
                NavHostFragment.findNavController(this).popBackStack());
        render();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh after navigating back from another Seller feature.
        if (binding != null && repository != null) render();
    }

    private void render() {
        Order order = orderId == null || orderId.trim().isEmpty()
                ? null : repository.getOrder(orderId);
        binding.sellerDetailContent.setVisibility(order == null ? View.GONE : View.VISIBLE);
        binding.tvSellerDetailNotFound.setVisibility(order == null ? View.VISIBLE : View.GONE);
        if (order == null) return;

        binding.tvSellerDetailOrderId.setText(getString(
                R.string.thien_seller_detail_order_id, order.orderId));
        binding.tvSellerDetailDate.setText(formatDate(order.createdAt));
        binding.tvSellerDetailCustomer.setText(displayValue(order.customerName));
        binding.tvSellerDetailTable.setText(order.tableNumber == null || order.tableNumber.trim().isEmpty()
                ? getString(R.string.thien_seller_detail_no_table)
                : getString(R.string.thien_seller_detail_table_number, order.tableNumber));
        binding.tvSellerDetailPayment.setText(paymentLabel(order.paymentMethod));
        showStatus(order.status);

        binding.llSellerDetailItems.removeAllViews();
        binding.tvSellerDetailEmptyItems.setVisibility(
                order.items.isEmpty() ? View.VISIBLE : View.GONE);
        int count = 0;
        for (OrderItem item : order.items) {
            count += item.quantity;
            ItemSellerOrderDetailProductBinding row = ItemSellerOrderDetailProductBinding.inflate(
                    getLayoutInflater(), binding.llSellerDetailItems, false);
            row.tvSellerDetailItemTitle.setText(getString(
                    R.string.thien_seller_detail_item_title, item.quantity, item.productName));
            row.tvSellerDetailItemPrice.setText(money(item.totalPrice()));
            row.tvSellerDetailItemOptions.setText(getString(
                    R.string.thien_seller_detail_item_options, item.size,
                    item.sugarPercent, item.icePercent));
            if (item.toppings.isEmpty()) {
                row.tvSellerDetailItemToppings.setVisibility(View.GONE);
            } else {
                row.tvSellerDetailItemToppings.setVisibility(View.VISIBLE);
                row.tvSellerDetailItemToppings.setText(getString(
                        R.string.thien_seller_detail_item_toppings, item.toppingLabel()));
            }
            row.tvSellerDetailItemUnitPrice.setText(getString(
                    R.string.thien_seller_detail_item_unit_price, money(item.unitPrice())));
            binding.llSellerDetailItems.addView(row.getRoot());
        }
        binding.tvSellerDetailItemCount.setText(getString(
                R.string.thien_seller_detail_item_count, count));
        boolean hasDiscount = order.discount > 0L;
        binding.llSellerDetailDiscount.setVisibility(hasDiscount ? View.VISIBLE : View.GONE);
        if (hasDiscount) {
            binding.tvSellerDetailDiscountTitle.setText(order.voucherCode == null ||
                    order.voucherCode.trim().isEmpty()
                    ? getString(R.string.thien_seller_detail_discount)
                    : getString(R.string.thien_seller_detail_discount_code, order.voucherCode));
            binding.tvSellerDetailDiscount.setText(getString(
                    R.string.thien_seller_detail_discount_amount, money(order.discount)));
        }
        binding.tvSellerDetailTotal.setText(money(order.totalAmount()));
        showReview(repository.getReview(order.orderId));
    }

    private void showStatus(OrderStatus status) {
        final int background;
        final int foreground;
        final int text;
        if (status == null) status = OrderStatus.PENDING;
        switch (status) {
            case COMPLETED:
                text = R.string.thien_seller_detail_status_complete;
                background = R.color.thien_seller_detail_success_bg;
                foreground = R.color.thien_seller_detail_success_text;
                break;
            case CANCELLED:
            case REJECTED:
                text = status == OrderStatus.REJECTED
                        ? R.string.thien_seller_detail_status_rejected
                        : R.string.thien_seller_detail_status_cancelled;
                background = R.color.thien_seller_detail_danger_bg;
                foreground = R.color.thien_seller_detail_danger_text;
                break;
            case PENDING:
                text = R.string.thien_seller_detail_status_pending;
                background = R.color.thien_seller_detail_pending_bg;
                foreground = R.color.thien_seller_detail_pending_text;
                break;
            case ACCEPTED:
                text = R.string.thien_seller_detail_status_accepted;
                background = R.color.thien_seller_detail_pending_bg;
                foreground = R.color.thien_seller_detail_pending_text;
                break;
            case READY:
                text = R.string.thien_seller_detail_status_ready;
                background = R.color.thien_seller_detail_pending_bg;
                foreground = R.color.thien_seller_detail_pending_text;
                break;
            case PREPARING:
            case DELIVERING:
            default:
                text = R.string.thien_seller_detail_status_preparing;
                background = R.color.thien_seller_detail_pending_bg;
                foreground = R.color.thien_seller_detail_pending_text;
                break;
        }
        binding.tvSellerDetailStatus.setText(text);
        binding.tvSellerDetailStatus.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), background)));
        binding.tvSellerDetailStatus.setTextColor(
                ContextCompat.getColor(requireContext(), foreground));
    }

    private void showReview(@Nullable Review review) {
        boolean hasReview = review != null;
        binding.cardSellerDetailReview.setVisibility(hasReview ? View.VISIBLE : View.GONE);
        binding.tvSellerDetailNoReview.setVisibility(hasReview ? View.GONE : View.VISIBLE);
        if (!hasReview) return;

        String filled = getString(R.string.thien_seller_detail_star_filled);
        String empty = getString(R.string.thien_seller_detail_star_empty);
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) stars.append(i < review.rating ? filled : empty);
        binding.tvSellerDetailStars.setText(stars.toString());
        binding.tvSellerDetailRating.setText(getString(
                R.string.thien_seller_detail_rating, review.rating));
        binding.tvSellerDetailReviewCustomer.setText(displayValue(review.customerName));
        binding.tvSellerDetailReviewDate.setText(formatDate(review.createdAt));

        boolean hasTags = review.tags != null && !review.tags.isEmpty();
        binding.tvSellerDetailReviewTags.setVisibility(hasTags ? View.VISIBLE : View.GONE);
        if (hasTags) binding.tvSellerDetailReviewTags.setText(
                getString(R.string.thien_seller_detail_review_tags,
                        android.text.TextUtils.join(" · ", review.tags)));

        boolean hasComment = review.comment != null && !review.comment.trim().isEmpty();
        binding.tvSellerDetailReviewComment.setVisibility(hasComment ? View.VISIBLE : View.GONE);
        if (hasComment) binding.tvSellerDetailReviewComment.setText(review.comment);
        // imageUris are intentionally not rendered: GD03 is a read-only, text-first
        // layout; image loading requires agreed URI/storage access and a later task.
    }

    private String paymentLabel(@Nullable PaymentMethod payment) {
        if (payment == null) return getString(R.string.thien_seller_detail_payment_unknown);
        switch (payment) {
            case CASH: return getString(R.string.thien_seller_detail_payment_cash);
            case TRANSFER: return getString(R.string.thien_seller_detail_payment_transfer);
            case EWALLET: return getString(R.string.thien_seller_detail_payment_wallet);
            default: return getString(R.string.thien_seller_detail_payment_unknown);
        }
    }

    private String displayValue(@Nullable String value) {
        return value == null || value.trim().isEmpty()
                ? getString(R.string.thien_seller_detail_unknown) : value;
    }

    private String money(long amount) {
        return getString(R.string.thien_seller_detail_money,
                NumberFormat.getIntegerInstance(VIETNAMESE).format(amount));
    }

    private String formatDate(long timestamp) {
        if (timestamp <= 0L) return getString(R.string.thien_seller_detail_unknown);
        return new SimpleDateFormat("dd/MM/yyyy · HH:mm", VIETNAMESE).format(new Date(timestamp));
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
