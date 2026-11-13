package com.uitdrinkapp.app.ui.customer.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.uitdrinkapp.app.data.mock.MessageMockData;
import com.uitdrinkapp.app.data.model.ChatThread;
import com.uitdrinkapp.app.data.model.Notification;
import com.uitdrinkapp.app.data.model.User;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentNotificationBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Một luồng thông báo duy nhất cho người mua (gồm cả tin nhắn từ quán), nhóm theo ngày. */
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
                NavHostFragment.findNavController(this).popBackStack());
        binding.rvNotif.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.tvNotifReadAll.setOnClickListener(v -> {
            RepositoryProvider.get(requireContext()).notifications()
                    .markAllAsRead(Notification.RecipientType.BUYER, currentUserId());
            load();
        });
        load();
    }

    private String currentUserId() {
        User user = RepositoryProvider.get(requireContext()).auth().getCurrentUser();
        return user == null ? "" : user.id;
    }

    private void load() {
        RepositoryProvider repos = RepositoryProvider.get(requireContext());
        List<NotificationRowAdapter.Row> items = new ArrayList<>();
        for (Notification n : repos.notifications()
                .getNotifications(Notification.RecipientType.BUYER, currentUserId())) {
            items.add(new NotificationRowAdapter.Row(n.notificationId, n.title, n.body, n.createdAt, !n.isRead));
        }
        for (ChatThread t : MessageMockData.THREADS) {
            items.add(new NotificationRowAdapter.Row(t.id, t.shopName, t.lastMessage,
                    t.lastMessageAt, t.unreadCount > 0));
        }
        items.sort((a, b) -> Long.compare(b.time, a.time));

        List<NotificationRowAdapter.Row> rows = new ArrayList<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String lastDay = null;
        for (NotificationRowAdapter.Row row : items) {
            String day = dayFormat.format(new Date(row.time));
            if (!day.equals(lastDay)) {
                rows.add(NotificationRowAdapter.Row.dateHeader(day));
                lastDay = day;
            }
            rows.add(row);
        }

        binding.rvNotif.setAdapter(new NotificationRowAdapter(rows, row -> {
            repos.notifications().markAsRead(row.id);   // tin nhắn không có trong repo: lệnh này bỏ qua
            load();
        }));
        binding.tvNotifEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
