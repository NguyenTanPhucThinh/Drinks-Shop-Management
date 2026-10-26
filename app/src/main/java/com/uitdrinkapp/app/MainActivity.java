package com.uitdrinkapp.app;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.res.ColorStateList;
import androidx.core.content.ContextCompat;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.snackbar.Snackbar;
import com.uitdrinkapp.app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Snackbar messageBar;
    private NavController nav;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, keyboard.bottom));
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(binding.getRoot());
        NavHostFragment host = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (host == null) throw new IllegalStateException("Missing navigation host");
        nav = host.getNavController();
        nav.addOnDestinationChangedListener((controller, destination, arguments) -> {
            binding.llBottomNav.setVisibility((destination.getId() == R.id.homeFragment || destination.getId() == R.id.vouchersFragment) ? View.VISIBLE : View.GONE);
            selectTab(binding.btnNavHome, destination.getId() == R.id.homeFragment);
            selectTab(binding.btnNavVouchers, destination.getId() == R.id.vouchersFragment);
            if (messageBar != null) messageBar.dismiss();
        });
        binding.btnNavHome.setOnClickListener(view -> nav.popBackStack(R.id.homeFragment, false));
        binding.btnNavVouchers.setOnClickListener(view -> openVouchers());
    }
    public void openVouchers() {
        if (nav.getCurrentDestination() != null && nav.getCurrentDestination().getId() == R.id.homeFragment)
            nav.navigate(R.id.vouchersFragment);
    }
    private void selectTab(ViewGroup tab, boolean selected) {
        tab.setSelected(selected);
        int color = ContextCompat.getColor(this, selected ? R.color.home_green : R.color.home_muted);
        ((ImageView) tab.getChildAt(0)).setImageTintList(ColorStateList.valueOf(color));
        ((TextView) tab.getChildAt(1)).setTextColor(color);
    }
    public void showMessage(int message) {
        if (messageBar == null) messageBar = Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT);
        messageBar.setText(message);
        messageBar.setAnchorView(binding.llBottomNav.getVisibility() == View.VISIBLE ? binding.llBottomNav : null);
        messageBar.show();
    }
}
