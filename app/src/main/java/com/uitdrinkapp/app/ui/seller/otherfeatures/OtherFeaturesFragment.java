package com.uitdrinkapp.app.ui.seller.otherfeatures;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.databinding.FragmentSellerOtherFeaturesBinding;

public final class OtherFeaturesFragment extends Fragment {
    private FragmentSellerOtherFeaturesBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSellerOtherFeaturesBinding.inflate(inflater, container, false);
        binding.btnSellerFeaturesBack.setOnClickListener(v -> requireActivity()
                .getOnBackPressedDispatcher().onBackPressed());
        bindFeature(binding.cardSellerStatistic.getRoot(), R.string.seller_feature_statistic,
                R.string.seller_feature_statistic_subtitle, R.drawable.ic_seller_statistic,
                R.id.sellerStatisticFragment);
        bindFeature(binding.cardSellerSalesHistory.getRoot(), R.string.seller_feature_sales_history,
                R.string.seller_feature_sales_history_subtitle, R.drawable.ic_seller_history,
                R.id.sellerHistoryFragment);
        bindFeature(binding.cardSellerVouchers.getRoot(), R.string.seller_feature_vouchers,
                R.string.seller_feature_vouchers_subtitle, R.drawable.ic_seller_voucher,
                R.id.sellerVouchersFragment);
        return binding.getRoot();
    }

    private void bindFeature(android.view.View card, int title, int subtitle, int icon,
            int destination) {
        android.widget.ImageView iconView = card.findViewById(R.id.iv_seller_feature_icon);
        android.widget.TextView titleView = card.findViewById(com.uitdrinkapp.app.R.id.tv_seller_feature_title);
        android.widget.TextView subtitleView = card.findViewById(com.uitdrinkapp.app.R.id.tv_seller_feature_subtitle);
        iconView.setImageResource(icon);
        titleView.setText(title);
        subtitleView.setText(subtitle);
        card.setContentDescription(getString(title) + ", " + getString(subtitle));
        card.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(destination));
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
