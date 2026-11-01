package com.uitdrinkapp.app.ui.customer.review;

import android.os.Bundle;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.chip.Chip;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.databinding.FragmentThienReviewBinding;
import com.uitdrinkapp.app.ui.customer.order.BuyerNavBars;
import com.uitdrinkapp.app.ui.customer.order.data.BuyerOrderRepository;
import com.uitdrinkapp.app.ui.customer.order.data.BuyerOrderRepositoryProvider;
import com.uitdrinkapp.app.ui.customer.order.data.DemoOrder;
import com.uitdrinkapp.app.ui.customer.order.data.DemoReview;
import java.util.ArrayList;
import java.util.List;

/** Màn 11: đánh giá 0..5 sao vàng, đa chọn tag, ảnh mock và xem lại đánh giá đã lưu. */
public final class ReviewFragment extends Fragment {
    private FragmentThienReviewBinding binding;
    private BuyerOrderRepository repo;
    private String orderId;
    private DemoReview savedReview;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                   @Nullable ViewGroup container,
                                                   @Nullable Bundle state) {
        binding = FragmentThienReviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        repo = BuyerOrderRepositoryProvider.get(requireContext());
        orderId = getArguments() == null ? null : getArguments().getString("order_id");
        DemoOrder order = orderId == null ? null : repo.getOrder(orderId);
        savedReview = orderId == null ? null : repo.getReview(orderId);
        boolean eligible = order != null && order.status == DemoOrder.Status.COMPLETED;
        binding.thienReviewContent.setVisibility(eligible ? View.VISIBLE : View.GONE);
        binding.tvThienReviewUnavailable.setVisibility(eligible ? View.GONE : View.VISIBLE);
        if (order != null) {
            binding.tvThienReviewOrder.setText(getString(R.string.thien_order_label, order.orderId)
                    + " · " + com.uitdrinkapp.app.ui.customer.order.OrderUiFormat.date(order.createdAt));
            if (!order.items.isEmpty()) binding.tvThienReviewProduct.setText(order.items.get(0).productName);
        }
        // Đúng phiếu xác nhận: bắt đầu 0 sao, không chọn sẵn nhận xét.
        binding.ratingThienStars.setRating(0f);
        for (int i = 0; i < binding.thienReviewTagGroup.getChildCount(); i++) {
            View child = binding.thienReviewTagGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setChecked(false);
                styleTag(chip);
                chip.setOnCheckedChangeListener((button, checked) -> styleTag(chip));
            }
        }
        binding.btnThienReviewBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnThienReviewImage.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.thien_review_mock_image, Toast.LENGTH_SHORT).show());
        if (savedReview != null) renderSavedReview(savedReview);
        else binding.btnThienSendReview.setOnClickListener(v -> submit());
    }
    @Override public void onResume() {
        super.onResume(); BuyerNavBars.setVisible(requireActivity(), false);
    }
    private void renderSavedReview(DemoReview review) {
        binding.ratingThienStars.setRating(review.stars);
        binding.ratingThienStars.setIsIndicator(true);
        binding.etThienReviewNote.setText(review.note);
        binding.etThienReviewNote.setEnabled(false);
        for (int i = 0; i < binding.thienReviewTagGroup.getChildCount(); i++) {
            View child = binding.thienReviewTagGroup.getChildAt(i);
            if (!(child instanceof Chip)) continue;
            Chip chip = (Chip) child;
            chip.setChecked(review.tags.contains(chip.getText().toString()));
            styleTag(chip);
            chip.setEnabled(false);
        }
        binding.btnThienReviewImage.setVisibility(View.GONE);
        binding.btnThienSendReview.setText(R.string.thien_review_already_sent);
        binding.btnThienSendReview.setEnabled(false);
    }
    private void styleTag(Chip chip) {
        int color = requireContext().getColor(chip.isChecked() ? R.color.primary : R.color.thien_white);
        int text = requireContext().getColor(chip.isChecked() ? R.color.on_primary : R.color.text_primary);
        chip.setChipBackgroundColor(ColorStateList.valueOf(color));
        chip.setTextColor(text);
    }
    private void submit() {
        int stars = (int) binding.ratingThienStars.getRating();
        if (stars < 1) {
            Toast.makeText(requireContext(), R.string.thien_review_stars_required, Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < binding.thienReviewTagGroup.getChildCount(); i++) {
            View child = binding.thienReviewTagGroup.getChildAt(i);
            if (child instanceof Chip && ((Chip) child).isChecked())
                tags.add(((Chip) child).getText().toString());
        }
        String note = binding.etThienReviewNote.getText() == null ? "" :
                binding.etThienReviewNote.getText().toString().trim();
        try {
            repo.submitReview(new DemoReview(orderId, stars, tags, note, ""));
            Toast.makeText(requireContext(), R.string.thien_review_saved, Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigate(R.id.action_thien_review_to_history);
        } catch (IllegalStateException | IllegalArgumentException e) {
            Toast.makeText(requireContext(), R.string.thien_review_unavailable, Toast.LENGTH_SHORT).show();
        }
    }
    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
