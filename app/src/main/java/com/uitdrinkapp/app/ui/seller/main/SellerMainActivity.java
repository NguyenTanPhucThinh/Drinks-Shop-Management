package com.uitdrinkapp.app.ui.seller.main;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.content.res.ColorStateList;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.databinding.ActivitySellerMainBinding;
import com.uitdrinkapp.app.databinding.ItemSellerNavBinding;
import java.util.LinkedHashMap;
import java.util.Map;

/** Seller shell. Feature screens keep their own layouts and stable destination IDs. */
public final class SellerMainActivity extends AppCompatActivity {
    private ActivitySellerMainBinding binding;
    private NavController nav;
    private int bottomSystemInset;
    private final Map<Integer, ItemSellerNavBinding> tabs = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySellerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);
        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightNavigationBars(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            bottomSystemInset = bars.bottom;
            int active = nav == null || nav.getCurrentDestination() == null ? -1
                    : nav.getCurrentDestination().getId();
            boolean hiddenBottomNav = nav != null && isBottomNavHidden(active);
            view.setPadding(bars.left, bars.top, bars.right,
                    hiddenBottomNav && active != R.id.sellerVouchersFragment
                            ? bottomSystemInset : 0);
            binding.llSellerBottomNav.setPadding(0, 0, 0, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(binding.getRoot());

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fcv_seller_nav_host);
        if (host == null) {
            throw new IllegalStateException("Missing seller navigation host");
        }
        nav = host.getNavController();
        bindTab(binding.llSellerNavHome, R.id.sellerHomeFragment,
                R.string.seller_title_home, R.drawable.ic_seller_home);
        bindTab(binding.llSellerNavOrders, R.id.sellerOrdersFragment,
                R.string.seller_title_orders, R.drawable.ic_seller_orders);
        bindTab(binding.llSellerNavProduct, R.id.sellerProductFragment,
                R.string.seller_title_product, R.drawable.ic_seller_product);
        bindTab(binding.llSellerNavOtherFeatures, R.id.sellerOtherFeaturesFragment,
                R.string.seller_tab_other_features, R.drawable.ic_seller_other_features);
        bindTab(binding.llSellerNavProfile, R.id.sellerProfileFragment,
                R.string.seller_title_profile, R.drawable.ic_seller_profile);

        nav.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int active = destination.getId();
            boolean hiddenBottomNav = isBottomNavHidden(active);
            binding.llSellerBottomNav.setVisibility(hiddenBottomNav ? View.GONE : View.VISIBLE);
            binding.getRoot().setPadding(binding.getRoot().getPaddingLeft(),
                    binding.getRoot().getPaddingTop(), binding.getRoot().getPaddingRight(),
                    hiddenBottomNav && active != R.id.sellerVouchersFragment
                            ? bottomSystemInset : 0);
            // Detail inherits the tab of the screen it was opened from.
            int selectedDestination = getParentTab(active);
            if (active == R.id.sellerOrderDetailFragment) {
                androidx.navigation.NavBackStackEntry previous = controller.getPreviousBackStackEntry();
                if (previous != null) {
                    selectedDestination = getParentTab(previous.getDestination().getId());
                }
            }
            int selectedColor = ContextCompat.getColor(this, R.color.seller_nav_active);
            int idleColor = ContextCompat.getColor(this, R.color.seller_nav_inactive);
            for (Map.Entry<Integer, ItemSellerNavBinding> entry : tabs.entrySet()) {
                boolean selected = entry.getKey() == selectedDestination;
                ItemSellerNavBinding tab = entry.getValue();
                tab.getRoot().setSelected(selected);
                int color = selected ? selectedColor : idleColor;
                tab.ivSellerNavIcon.setImageTintList(ColorStateList.valueOf(color));
                tab.tvSellerNavLabel.setTextColor(color);
            }
            setTitle(destination.getLabel());
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int current = nav.getCurrentDestination() == null ? -1
                        : nav.getCurrentDestination().getId();
                // Detail pages return to Orders; other secondary screens return to their parent tab.
                if (current == R.id.sellerOrderDetailFragment) {
                    if (!nav.popBackStack()) openTab(R.id.sellerOrdersFragment);
                    return;
                }
                if (current == R.id.sellerHomeFragment || current == -1) {
                    finish();
                } else if (isPrimaryTab(current)) {
                    openTab(R.id.sellerHomeFragment);
                } else if (!nav.popBackStack()) {
                    int parentTab = getParentTab(current);
                    if (parentTab != -1) openTab(parentTab);
                    else finish();
                }
            }
        });
    }

    private void bindTab(ItemSellerNavBinding tab, int destination, int title, int icon) {
        tab.tvSellerNavLabel.setText(title);
        tab.ivSellerNavIcon.setImageResource(icon);
        tab.getRoot().setContentDescription(getString(title));
        tab.getRoot().setOnClickListener(view -> openTab(destination));
        tabs.put(destination, tab);
    }

    public void openTab(int destination) {
        if (nav.getCurrentDestination() == null
                || nav.getCurrentDestination().getId() == destination) {
            return;
        }
        NavOptions options = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(R.id.sellerHomeFragment, false, true)
                .build();
        nav.navigate(destination, null, options);
    }

    private boolean isBottomNavHidden(int destination) {
        return destination == R.id.sellerEditProductFragment
                || destination == R.id.sellerVouchersFragment
                || destination == R.id.sellerVoucherFormFragment;
    }

    private int getParentTab(int destination) {
        if (destination == R.id.sellerOrderDetailFragment) return R.id.sellerOrdersFragment;
        if (destination == R.id.sellerEditProductFragment) return R.id.sellerProductFragment;
        if (destination == R.id.sellerStatisticFragment
                || destination == R.id.sellerHistoryFragment
                || destination == R.id.sellerVouchersFragment
                || destination == R.id.sellerVoucherFormFragment) {
            return R.id.sellerOtherFeaturesFragment;
        }
        return destination;
    }

    private boolean isPrimaryTab(int destination) {
        return destination == R.id.sellerHomeFragment
                || destination == R.id.sellerNotificationFragment
                || destination == R.id.sellerOrdersFragment
                || destination == R.id.sellerProductFragment
                || destination == R.id.sellerOtherFeaturesFragment
                || destination == R.id.sellerProfileFragment;
    }
}
