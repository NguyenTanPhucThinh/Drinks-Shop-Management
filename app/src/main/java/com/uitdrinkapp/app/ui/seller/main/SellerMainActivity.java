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
            view.setPadding(bars.left, bars.top, bars.right, 0);
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
        bindTab(binding.llSellerNavNotification, R.id.sellerNotificationFragment,
                R.string.seller_title_notification, R.drawable.ic_seller_notification);
        bindTab(binding.llSellerNavOrders, R.id.sellerOrdersFragment,
                R.string.seller_title_orders, R.drawable.ic_seller_orders);
        bindTab(binding.llSellerNavProduct, R.id.sellerProductFragment,
                R.string.seller_title_product, R.drawable.ic_seller_product);
        bindTab(binding.llSellerNavOtherFeatures, R.id.sellerOtherFeaturesFragment,
                R.string.seller_tab_other_features, R.drawable.ic_seller_other_features);
        bindTab(binding.llSellerNavProfile, R.id.sellerProfileFragment,
                R.string.seller_title_profile, R.drawable.ic_seller_profile);

        // Static unread indicator for the shell preview; real notification data comes later.
        binding.llSellerNavNotification.vSellerNavBadge.setVisibility(View.VISIBLE);
        binding.llSellerNavNotification.getRoot().setContentDescription(
                getString(R.string.seller_notification_unread));

        nav.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int active = destination.getId();
            int selectedColor = ContextCompat.getColor(this, R.color.seller_nav_active);
            int idleColor = ContextCompat.getColor(this, R.color.seller_nav_inactive);
            for (Map.Entry<Integer, ItemSellerNavBinding> entry : tabs.entrySet()) {
                boolean selected = entry.getKey() == active;
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
                if (nav.getCurrentDestination() != null
                        && nav.getCurrentDestination().getId() != R.id.sellerHomeFragment) {
                    if (!nav.popBackStack(R.id.sellerHomeFragment, false)) {
                        openTab(R.id.sellerHomeFragment);
                    }
                } else {
                    finish();
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

    private void openTab(int destination) {
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
}
