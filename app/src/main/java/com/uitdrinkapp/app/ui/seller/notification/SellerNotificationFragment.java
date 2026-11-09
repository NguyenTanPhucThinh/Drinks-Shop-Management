package com.uitdrinkapp.app.ui.seller.notification;

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
import android.widget.Toast;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.Notification;
import com.uitdrinkapp.app.data.model.User;
import com.uitdrinkapp.app.data.order.OrderRepository;
import com.uitdrinkapp.app.data.order.OrderRepositoryProvider;
import com.uitdrinkapp.app.data.repository.FakeNotificationRepository;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentSellerNotificationBinding;
import com.uitdrinkapp.app.ui.customer.notification.NotificationRowAdapter;
import java.util.ArrayList;
import java.util.List;

public final class SellerNotificationFragment extends Fragment {
    private FragmentSellerNotificationBinding binding;
    private RepositoryProvider repositories;
    private OrderRepository orders;
    private NotificationRowAdapter adapter;
    private String sellerId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSellerNotificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repositories = RepositoryProvider.get(requireContext());
        orders = OrderRepositoryProvider.get(requireContext());
        User user = repositories.auth().getCurrentUser();
        sellerId = user == null ? FakeNotificationRepository.DEMO_SELLER_ID : user.id;
        adapter = new NotificationRowAdapter(new ArrayList<>(), this::openNotification);
        binding.rvSellerNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSellerNotifications.setAdapter(adapter);
        refreshNotifications();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && adapter != null && repositories != null) refreshNotifications();
    }

    private void refreshNotifications() {
        List<NotificationRowAdapter.Row> rows = new ArrayList<>();
        for (Notification notification : repositories.notifications().getNotifications(
                Notification.RecipientType.SELLER, sellerId)) {
            rows.add(new NotificationRowAdapter.Row(notification.notificationId,
                    notification.title, notification.body, notification.createdAt,
                    !notification.isRead));
        }
        adapter.replace(rows);
        boolean empty = rows.isEmpty();
        binding.tvSellerNotificationsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvSellerNotifications.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void openNotification(NotificationRowAdapter.Row row) {
        Notification notification = null;
        for (Notification item : repositories.notifications().getNotifications(
                Notification.RecipientType.SELLER, sellerId)) {
            if (item.notificationId.equals(row.id)) {
                notification = item;
                break;
            }
        }
        if (notification == null) return;
        repositories.notifications().markAsRead(notification.notificationId);
        refreshNotifications();
        if (notification.referenceType != Notification.ReferenceType.ORDER
                || notification.referenceId.trim().isEmpty()) return;
        if (orders.getOrder(notification.referenceId) == null) {
            Toast.makeText(requireContext(), R.string.thien_seller_orders_not_found,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        NavController nav = NavHostFragment.findNavController(this);
        if (nav.getCurrentDestination() == null
                || nav.getCurrentDestination().getId() != R.id.sellerNotificationFragment) return;
        Bundle args = new Bundle();
        args.putString("order_id", notification.referenceId);
        nav.navigate(R.id.action_sellerNotification_to_orderDetail, args);
    }

    @Override
    public void onDestroyView() {
        if (binding != null) binding.rvSellerNotifications.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }
}
