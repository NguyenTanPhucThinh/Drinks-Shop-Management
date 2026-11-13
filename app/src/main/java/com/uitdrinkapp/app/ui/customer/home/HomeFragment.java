package com.uitdrinkapp.app.ui.customer.home;


import android.os.Bundle;
import com.uitdrinkapp.app.data.model.Notification;
import com.uitdrinkapp.app.data.model.User;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager2.widget.ViewPager2;
import com.uitdrinkapp.app.MainActivity;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.Product;
import com.uitdrinkapp.app.data.model.Shop;
import com.uitdrinkapp.app.data.model.ShopSession;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentHomeBinding;
import com.uitdrinkapp.app.ui.seller.main.SellerMainActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public final class HomeFragment extends Fragment {
    public static final String JOIN_RESULT = "join_result";
    private static final int HOME_PREVIEW_COUNT = 2;
    private FragmentHomeBinding binding;
    private RepositoryProvider repositories;
    private ViewPager2.OnPageChangeCallback bannerCallback;
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private final Runnable advanceBanner = () -> {
        if (binding != null && isResumed() && binding.vpBanners.getAdapter() != null) {
            binding.vpBanners.setCurrentItem(binding.vpBanners.getCurrentItem() + 1, true);
            scheduleBannerAdvance();
        }
    };
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        repositories = RepositoryProvider.get(requireContext());

        // Mở màn Thông báo + Tin nhắn khi bấm chuông.
        binding.btnNotifications.setOnClickListener(clicked -> {
            NavController nav = NavHostFragment.findNavController(this);
            if (nav.getCurrentDestination() != null
                    && nav.getCurrentDestination().getId() == R.id.homeFragment) {
                nav.navigate(R.id.notificationFragment);
            }
        });
        binding.cardShopInfo.setOnClickListener(clicked -> {
            NavController nav = NavHostFragment.findNavController(this);
            if (nav.getCurrentDestination() != null
                    && nav.getCurrentDestination().getId() == R.id.homeFragment) {
                nav.navigate(R.id.action_home_to_shop_info);
            }
        });
        binding.btnScanQr.setOnClickListener(clicked -> {
            if (NavHostFragment.findNavController(this).getCurrentDestination().getId() == R.id.homeFragment)
                NavHostFragment.findNavController(this).navigate(R.id.action_home_to_join);
        });
        binding.btnFeaturedAll.setOnClickListener(clicked -> openMenu());
        binding.btnRecommendedAll.setOnClickListener(clicked -> openMenu());
        binding.rvFeatured.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvRecommended.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        refreshProductGrids();
        int bannerCount = repositories.home().getBanners().size();
        binding.vpBanners.setAdapter(new BannerAdapter(repositories.home().getBanners(), () -> ((MainActivity) requireActivity()).openVouchers()));
        if (bannerCount > 1) {
            int middlePosition = Integer.MAX_VALUE / 2;
            binding.vpBanners.setCurrentItem(middlePosition - middlePosition % bannerCount, false);
        }
        bannerCallback = new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) { scheduleBannerAdvance(); }
        };
        binding.vpBanners.registerOnPageChangeCallback(bannerCallback);
        NavBackStackEntry entry = NavHostFragment.findNavController(this).getCurrentBackStackEntry();
        if (entry != null) entry.getSavedStateHandle().<Integer>getLiveData(JOIN_RESULT).observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                entry.getSavedStateHandle().remove(JOIN_RESULT);
                ((MainActivity) requireActivity()).showMessage(message);
            }
        });
    }
    private List<Product> products(List<String> ids) {
        List<Product> result = new ArrayList<>();
        for (String id : ids) {
            Product product = repositories.products().getProductById(id);
            if (product != null) result.add(product);
        }
        return result;
    }
    /** Trang chủ chỉ hiện vài món; "Xem tất cả" mở màn Menu với đầy đủ sản phẩm. */
    private void refreshProductGrids() {
        if (binding == null || repositories == null) return;
        List<Product> featured = products(repositories.home().getFeaturedProductIds());
        List<Product> recommended = new ArrayList<>();
        List<String> featuredIds = repositories.home().getFeaturedProductIds();
        for (Product p : repositories.products().getProducts()) {
            if (!featuredIds.contains(p.getId())) recommended.add(p);
        }
        if (recommended.isEmpty()) recommended.addAll(repositories.products().getProducts());
        binding.rvFeatured.setAdapter(new ProductAdapter(limit(featured), this::openProduct));
        binding.rvRecommended.setAdapter(new ProductAdapter(limit(recommended), this::openProduct));
    }
    private static List<Product> limit(List<Product> all) {
        return new ArrayList<>(all.subList(0, Math.min(HOME_PREVIEW_COUNT, all.size())));
    }
    private void openMenu() {
        NavController nav = NavHostFragment.findNavController(this);
        if (nav.getCurrentDestination() != null
                && nav.getCurrentDestination().getId() == R.id.homeFragment) {
            nav.navigate(R.id.menuFragment);
        }
    }
    private void scheduleBannerAdvance() {
        bannerHandler.removeCallbacks(advanceBanner);
        if (binding != null && isResumed() && repositories.home().getBanners().size() > 1) {
            bannerHandler.postDelayed(advanceBanner, getResources().getInteger(R.integer.home_banner_interval_ms));
        }
    }
    private void openProduct(String productId) {
        androidx.navigation.NavController nav = NavHostFragment.findNavController(this);
        if (nav.getCurrentDestination() != null && nav.getCurrentDestination().getId() == R.id.homeFragment) {
            Bundle args = new Bundle();
            args.putString(com.uitdrinkapp.app.ui.customer.product.ProductDetailFragment.ARG_PRODUCT_ID, productId);
            nav.navigate(R.id.productDetailFragment, args);
        }
    }
    @Override public void onResume() {
        super.onResume();
        refreshProductGrids();
        refreshNotificationBadge();
        scheduleBannerAdvance();
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        binding.tvGreeting.setText(hour < 12 ? R.string.home_greeting_morning : hour < 18 ? R.string.home_greeting_afternoon : R.string.home_greeting_evening);
        ShopSession session = repositories.sessions().getSession();
        Shop shop = session == null ? repositories.shops().findByCode(repositories.shops().getDemoCode()) : repositories.shops().findById(session.getShopId());
        binding.tvShopName.setText(shop.getName());
        if (session == null) binding.tvShopStatus.setText(R.string.home_not_joined);
        else if (session.getTableNumber().isEmpty()) binding.tvShopStatus.setText(R.string.home_joined);
        else binding.tvShopStatus.setText(getString(R.string.home_joined_table, session.getTableNumber()));
    }
    @Override public void onPause() {
        bannerHandler.removeCallbacks(advanceBanner);
        super.onPause();
    }
    @Override public void onDestroyView() {
        binding.vpBanners.unregisterOnPageChangeCallback(bannerCallback);
        binding.vpBanners.setAdapter(null);
        binding.rvFeatured.setAdapter(null);
        binding.rvRecommended.setAdapter(null);
        binding = null;
        super.onDestroyView();
    }
    /** Chấm đỏ trên chuông chỉ hiện khi còn thông báo chưa đọc. */
    private void refreshNotificationBadge() {
        if (binding == null) return;
        User user = repositories.auth().getCurrentUser();
        int unread = user == null ? 0
                : repositories.notifications().getUnreadCount(Notification.RecipientType.BUYER, user.id);
        binding.vHomeNotifBadge.setVisibility(unread > 0 ? View.VISIBLE : View.GONE);
    }
}