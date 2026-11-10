package com.uitdrinkapp.app.ui.seller.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.Order;
import com.uitdrinkapp.app.data.model.Shop;
import com.uitdrinkapp.app.data.order.OrderRepository;
import com.uitdrinkapp.app.data.order.OrderRepositoryProvider;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentSellerHomeBinding;
import com.uitdrinkapp.app.domain.OrderStatistics;
import com.uitdrinkapp.app.ui.seller.orders.SellerOrderAdapter;

import com.uitdrinkapp.app.ui.seller.main.SellerMainActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class SellerHomeFragment extends Fragment {
    private FragmentSellerHomeBinding binding;
    private OrderRepository orders;
    private SellerOrderAdapter adapter;
    private RepositoryProvider repositories;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSellerHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repositories = RepositoryProvider.get(requireContext());
        orders = OrderRepositoryProvider.get(requireContext());

        adapter = new SellerOrderAdapter((id, status) -> { }, this::openDetail, false);
        binding.rvSellerHomeOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSellerHomeOrders.setAdapter(adapter);

        binding.tvSellerHomeSeeAll.setOnClickListener(v -> {
            if (requireActivity() instanceof SellerMainActivity) {
                ((SellerMainActivity) requireActivity()).openTab(R.id.sellerOrdersFragment);
            }
        });

        refreshData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && adapter != null && orders != null) refreshData();
    }

    private void refreshData() {
        // Shop Name
        Shop shop = repositories.shops().findById("abc");
        if (shop != null) {
            binding.tvSellerHomeShopName.setText(shop.getName());
        }

        // Statistics
        OrderStatistics stats = OrderStatistics.fromOrders(orders.getOrders(), System.currentTimeMillis());
        binding.tvStatNewOrders.setText(String.valueOf(stats.pendingCount));
        binding.tvStatPreparing.setText(String.valueOf(stats.preparingCount));
        binding.tvStatCompleted.setText(String.valueOf(stats.completedCount));
        binding.tvStatRevenue.setText(String.format(Locale.getDefault(), "%,dđ", stats.todayRevenue));

        // Recent Orders
        List<Order> recent = new ArrayList<>(orders.getOrders());
        Collections.sort(recent, Comparator.comparingLong((Order order) -> order.createdAt)
                .reversed().thenComparing(order -> order.orderId));
        if (recent.size() > 5) recent = new ArrayList<>(recent.subList(0, 5));
        adapter.submit(recent);
        binding.tvSellerHomeOrdersEmpty.setVisibility(recent.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvSellerHomeOrders.setVisibility(recent.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void openDetail(String orderId) {
        if (orders.getOrder(orderId) == null) return;
        NavController nav = NavHostFragment.findNavController(this);
        if (nav.getCurrentDestination() == null
                || nav.getCurrentDestination().getId() != R.id.sellerHomeFragment) return;
        Bundle args = new Bundle();
        args.putString("order_id", orderId);
        nav.navigate(R.id.action_sellerHome_to_orderDetail, args);
    }

    @Override
    public void onDestroyView() {
        if (binding != null) binding.rvSellerHomeOrders.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }
}
