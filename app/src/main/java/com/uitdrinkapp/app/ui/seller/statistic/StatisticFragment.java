package com.uitdrinkapp.app.ui.seller.statistic;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.order.BuyerOrderRepositoryProvider;
import com.uitdrinkapp.app.data.repository.SellerStatisticsRepository;
import com.uitdrinkapp.app.databinding.FragmentSellerStatisticBinding;
import java.util.List;
import java.util.Locale;

public final class StatisticFragment extends Fragment {
    private FragmentSellerStatisticBinding binding;
    private int selectedPeriod;
    private SellerStatisticsRepository statisticsRepository;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentSellerStatisticBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        statisticsRepository = new SellerStatisticsRepository(
                BuyerOrderRepositoryProvider.get(requireContext()));
        binding.btnSellerStatisticBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        TextView[] chips = {binding.chipStatWeek, binding.chipStatMonth, binding.chipStatYear};
        for (int i = 0; i < chips.length; i++) {
            final int period = i;
            chips[i].setOnClickListener(v -> renderPeriod(period));
        }
        binding.btnStatAllRated.setOnClickListener(v -> showAll(binding.llStatRated));
        binding.btnStatAllCustomers.setOnClickListener(v -> showAll(binding.llStatCustomers));
        renderPeriod(selectedPeriod);
    }

    private void renderPeriod(int period) {
        selectedPeriod = period;
        TextView[] chips = {binding.chipStatWeek, binding.chipStatMonth, binding.chipStatYear};
        for (int i = 0; i < chips.length; i++) {
            boolean selected = i == period;
            chips[i].setSelected(selected);
            chips[i].setTextColor(ContextCompat.getColor(requireContext(), selected
                    ? android.R.color.white : R.color.seller_text_primary));
        }
        SellerStatisticsRepository.Report report = statisticsRepository.getReport(period);
        int revenueTitle = period == 0 ? R.string.seller_stat_revenue_week
                : period == 1 ? R.string.seller_stat_revenue_month
                : R.string.seller_stat_revenue_year;
        binding.tvStatRevenueTitle.setText(revenueTitle);
        binding.tvStatRevenue.setText(String.format(Locale.getDefault(), "%,dđ", report.revenue));
        binding.tvStatChange.setText(formatChange(report.revenue, report.previousRevenue));
        renderChart(report.chart);
        renderSelling(report.selling);
        renderRated(report.rated);
        renderCustomers(report.customers);
    }

    private void renderChart(List<SellerStatisticsRepository.ChartPoint> points) {
        binding.llStatChart.removeAllViews();
        int active = ContextCompat.getColor(requireContext(), R.color.seller_chart_bar_highlight);
        int normal = ContextCompat.getColor(requireContext(), R.color.seller_chart_bar);
        for (int i = 0; i < points.size(); i++) {
            LinearLayout column = new LinearLayout(requireContext());
            column.setOrientation(LinearLayout.VERTICAL);
            column.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams columnParams = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            binding.llStatChart.addView(column, columnParams);
            View bar = new View(requireContext());
            bar.setBackgroundColor(points.get(i).heightPercent >= 90 ? active : normal);
            int barHeight = Math.round(points.get(i).heightPercent
                    * getResources().getDisplayMetrics().density * .52f);
            column.addView(bar, new LinearLayout.LayoutParams(dp(21), barHeight));
            TextView label = new TextView(requireContext());
            label.setText(points.get(i).label);
            label.setTextColor(ContextCompat.getColor(requireContext(), R.color.seller_text_secondary));
            label.setTextSize(9);
            label.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(-1, dp(17));
            labelParams.topMargin = dp(3);
            column.addView(label, labelParams);
        }
    }

    private void renderSelling(List<SellerStatisticsRepository.ProductSales> products) {
        binding.llStatSelling.removeAllViews();
        if (products.isEmpty()) {
            addEmptyRow(binding.llStatSelling);
            return;
        }
        int max = 1;
        for (SellerStatisticsRepository.ProductSales product : products) {
            max = Math.max(max, product.quantity);
        }
        for (int i = 0; i < Math.min(3, products.size()); i++) {
            SellerStatisticsRepository.ProductSales product = products.get(i);
            addProgressRow(binding.llStatSelling, product.name,
                    getString(R.string.seller_stat_orders_count, product.quantity),
                    (float) product.quantity / max);
        }
    }

    private void renderRated(List<SellerStatisticsRepository.ProductRating> products) {
        binding.llStatRated.removeAllViews();
        if (products.isEmpty()) {
            addEmptyRow(binding.llStatRated);
            return;
        }
        for (SellerStatisticsRepository.ProductRating product : products) {
            String count = getString(R.string.seller_stat_ratings_count, product.count);
            addRankRow(binding.llStatRated, product.name, count,
                    String.format(Locale.getDefault(), "★ %.1f", product.average()));
        }
        collapseExtraRows(binding.llStatRated);
    }

    private void renderCustomers(List<SellerStatisticsRepository.CustomerSales> customers) {
        binding.llStatCustomers.removeAllViews();
        if (customers.isEmpty()) {
            addEmptyRow(binding.llStatCustomers);
            return;
        }
        for (SellerStatisticsRepository.CustomerSales customer : customers) {
            String count = getString(R.string.seller_stat_purchases_count, customer.orderCount);
            addCustomerRow(customer.name, count, formatMoney(customer.spent));
        }
        collapseExtraRows(binding.llStatCustomers);
    }

    private String formatMoney(long amount) {
        return String.format(Locale.getDefault(), "%,dđ", amount);
    }

    private String formatChange(long current, long previous) {
        if (previous <= 0) return "—";
        long percent = Math.round((current - previous) * 100.0 / previous);
        return String.format(Locale.getDefault(), "%+d%%", percent);
    }

    private void addEmptyRow(LinearLayout parent) {
        TextView empty = text(getString(R.string.seller_stat_no_data), 11,
                R.color.seller_text_secondary, false);
        empty.setPadding(0, dp(8), 0, dp(4));
        parent.addView(empty);
    }

    private void addCustomerRow(String name, String count, String amount) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(5), 0, dp(5));
        TextView icon = new TextView(requireContext());
        icon.setText("●");
        icon.setTextColor(ContextCompat.getColor(requireContext(), R.color.seller_nav_active));
        icon.setGravity(Gravity.CENTER);
        icon.setBackgroundResource(R.drawable.bg_seller_icon_circle);
        row.addView(icon, new LinearLayout.LayoutParams(dp(26), dp(26)));
        LinearLayout details = new LinearLayout(requireContext());
        details.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(0, -2, 1f);
        detailsParams.leftMargin = dp(8);
        row.addView(details, detailsParams);
        details.addView(text(name, 11, R.color.seller_text_primary, true));
        details.addView(text(count, 9, R.color.seller_text_secondary, false));
        TextView value = text(amount, 10, R.color.seller_nav_active, true);
        row.addView(value);
        binding.llStatCustomers.addView(row);
    }

    private void addRankRow(LinearLayout parent, String title, String subtitle, String score) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(5), 0, dp(5));
        TextView rank = text(String.valueOf(parent.getChildCount() + 1), 11,
                R.color.seller_nav_active, true);
        rank.setGravity(Gravity.CENTER);
        rank.setBackgroundResource(R.drawable.bg_seller_icon_circle);
        row.addView(rank, new LinearLayout.LayoutParams(dp(26), dp(26)));
        LinearLayout details = new LinearLayout(requireContext());
        details.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(0, -2, 1f);
        detailsParams.leftMargin = dp(8);
        row.addView(details, detailsParams);
        details.addView(text(title, 11, R.color.seller_text_primary, true));
        details.addView(text(subtitle, 9, R.color.seller_text_secondary, false));
        row.addView(text(score, 10, R.color.seller_nav_active, true));
        parent.addView(row);
    }

    private void addProgressRow(LinearLayout parent, String title, String count, float progress) {
        TextView name = text(title, 10, R.color.seller_text_primary, false);
        name.setPadding(0, dp(3), 0, 0);
        parent.addView(name);
        TextView value = text(count, 9, R.color.seller_nav_active, true);
        value.setGravity(Gravity.RIGHT);
        parent.addView(value);
        android.widget.ProgressBar bar = new android.widget.ProgressBar(requireContext(), null,
                android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(Math.round(progress * 100));
        bar.setProgressTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.seller_nav_active)));
        bar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.seller_surface_border)));
        parent.addView(bar, new LinearLayout.LayoutParams(-1, dp(4)));
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView result = new TextView(requireContext());
        result.setText(value);
        result.setTextSize(size);
        result.setTextColor(ContextCompat.getColor(requireContext(), color));
        if (bold) result.setTypeface(null, android.graphics.Typeface.BOLD);
        return result;
    }

    private void showAll(LinearLayout rows) {
        for (int i = rows.getChildCount() - 1; i >= 3; i--) rows.getChildAt(i).setVisibility(View.VISIBLE);
    }

    private void collapseExtraRows(LinearLayout rows) {
        for (int i = 3; i < rows.getChildCount(); i++) rows.getChildAt(i).setVisibility(View.GONE);
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override public void onDestroyView() { binding = null; super.onDestroyView(); }
}
