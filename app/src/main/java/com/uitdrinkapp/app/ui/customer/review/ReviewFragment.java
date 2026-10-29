package com.uitdrinkapp.app.ui.customer.review;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.chip.Chip;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.databinding.FragmentThienReviewBinding;
import com.uitdrinkapp.app.ui.customer.order.data.BuyerOrderRepository;
import com.uitdrinkapp.app.ui.customer.order.data.BuyerOrderRepositoryProvider;
import com.uitdrinkapp.app.ui.customer.order.data.DemoOrder;
import com.uitdrinkapp.app.ui.customer.order.data.DemoReview;
import java.util.ArrayList;
import java.util.List;

/** Màn 11: sao + thẻ nhận xét + góp ý + ảnh chọn từ thiết bị, lưu demo. */
public final class ReviewFragment extends Fragment {
    private FragmentThienReviewBinding binding;
    private BuyerOrderRepository repo;
    private String orderId;
    private Uri imageUri;
    private final ActivityResultLauncher<String[]> imagePicker = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                imageUri = uri;
                try {
                    requireContext().getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) { /* Provider có thể không hỗ trợ lưu quyền. */ }
                if (binding != null) {
                    binding.ivThienReviewImage.setImageURI(uri);
                    binding.ivThienReviewImage.setVisibility(View.VISIBLE);
                }
            });

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                    @Nullable ViewGroup container,
                                                    @Nullable Bundle savedInstanceState) {
        binding = FragmentThienReviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repo = BuyerOrderRepositoryProvider.get(requireContext());
        orderId = getArguments() == null ? null : getArguments().getString("order_id");
        DemoOrder order = orderId == null ? null : repo.getOrder(orderId);
        boolean eligible = order != null && order.status == DemoOrder.Status.COMPLETED
                && !repo.hasReview(orderId);
        binding.thienReviewContent.setVisibility(eligible ? View.VISIBLE : View.GONE);
        binding.tvThienReviewUnavailable.setVisibility(eligible ? View.GONE : View.VISIBLE);
        if (order != null) {
            binding.tvThienReviewOrder.setText(getString(R.string.thien_order_label, order.orderId));
            if (!order.items.isEmpty()) binding.tvThienReviewProduct.setText(order.items.get(0).productName);
        }
        binding.btnThienReviewBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnThienReviewImage.setOnClickListener(v -> imagePicker.launch(new String[]{"image/*"}));
        binding.btnThienSendReview.setOnClickListener(v -> submit());
    }
    private void submit() {
        int stars = (int) binding.ratingThienStars.getRating();
        if (stars < 1) {
            Toast.makeText(requireContext(), R.string.thien_choose_star, Toast.LENGTH_SHORT).show(); return;
        }
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < binding.thienReviewTagGroup.getChildCount(); i++) {
            View child = binding.thienReviewTagGroup.getChildAt(i);
            if (child instanceof Chip && ((Chip) child).isChecked()) tags.add(((Chip) child).getText().toString());
        }
        String message = binding.etThienReviewNote.getText() == null ? "" :
                binding.etThienReviewNote.getText().toString().trim();
        try {
            repo.submitReview(new DemoReview(orderId, stars, tags, message,
                    imageUri == null ? "" : imageUri.toString()));
            Toast.makeText(requireContext(), R.string.thien_review_saved, Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigate(R.id.action_thien_review_to_history);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            Toast.makeText(requireContext(), R.string.thien_review_unavailable, Toast.LENGTH_SHORT).show();
        }
    }
    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
