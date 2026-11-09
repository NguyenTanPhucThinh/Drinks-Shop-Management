package com.uitdrinkapp.app.ui.customer.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.mock.MessageMockData;
import com.uitdrinkapp.app.data.model.ChatThread;
import com.uitdrinkapp.app.data.model.Notification;
import com.uitdrinkapp.app.data.model.User;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentNotificationListBinding;
import java.util.ArrayList;
import java.util.List;

public class NotificationListFragment extends Fragment {
    private static final String ARG_KIND = "kind";
    public static final int KIND_NOTIFICATIONS = 0;
    public static final int KIND_MESSAGES = 1;

    private FragmentNotificationListBinding binding;
    private int kind;

    public static NotificationListFragment newInstance(int kind) {
        NotificationListFragment fragment = new NotificationListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_KIND, kind);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        kind = requireArguments().getInt(ARG_KIND, KIND_NOTIFICATIONS);
        binding.rvNotif.setLayoutManager(new LinearLayoutManager(requireContext()));
        load();
    }

    private void load() {
        RepositoryProvider repos = RepositoryProvider.get(requireContext());
        List<NotificationRowAdapter.Row> rows = new ArrayList<>();
        if (kind == KIND_NOTIFICATIONS) {
            User user = repos.auth().getCurrentUser();
            String userId = user == null ? "" : user.id;
            for (Notification n : repos.notifications()
                    .getNotifications(Notification.RecipientType.BUYER, userId)) {
                rows.add(new NotificationRowAdapter.Row(n.notificationId, n.title, n.body, n.createdAt, !n.isRead));
            }
        } else {
            for (ChatThread t : MessageMockData.THREADS) {
                rows.add(new NotificationRowAdapter.Row(t.id, t.shopName, t.lastMessage, t.lastMessageAt, t.unreadCount > 0));
            }
        }
        binding.rvNotif.setAdapter(new NotificationRowAdapter(rows, row -> {
            if (kind == KIND_NOTIFICATIONS) {
                repos.notifications().markAsRead(row.id);
                load();   // vẽ lại để chấm đỏ biến mất
            }
        }));
        binding.tvNotifEmpty.setText(kind == KIND_NOTIFICATIONS ? R.string.vy_notif_empty : R.string.vy_msg_empty);
        binding.tvNotifEmpty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}