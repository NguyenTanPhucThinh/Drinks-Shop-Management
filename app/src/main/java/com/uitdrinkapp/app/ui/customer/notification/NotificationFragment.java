package com.uitdrinkapp.app.ui.customer.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.android.material.tabs.TabLayoutMediator;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.databinding.FragmentNotificationBinding;

public class NotificationFragment extends Fragment {
    private FragmentNotificationBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnNotifBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.vpNotification.setAdapter(new FragmentStateAdapter(this) {
            @NonNull @Override public Fragment createFragment(int position) {
                return NotificationListFragment.newInstance(position);
            }
            @Override public int getItemCount() { return 2; }
        });
        new TabLayoutMediator(binding.tabNotification, binding.vpNotification, (tab, position) ->
                tab.setText(position == 0 ? R.string.vy_tab_notifications : R.string.vy_tab_messages)
        ).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}