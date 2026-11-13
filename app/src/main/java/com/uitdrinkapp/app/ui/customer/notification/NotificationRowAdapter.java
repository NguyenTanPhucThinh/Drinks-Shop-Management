package com.uitdrinkapp.app.ui.customer.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.uitdrinkapp.app.databinding.ItemNotificationBinding;
import com.uitdrinkapp.app.databinding.ItemNotificationDateBinding;
import java.util.List;

/** Danh sách thông báo gộp: dòng tiêu đề ngày xen kẽ với dòng thông báo. */
public final class NotificationRowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_DATE = 0;
    private static final int TYPE_ITEM = 1;

    public static final class Row {
        public final String id, title, body;
        public final long time;
        public final boolean unread;
        public final boolean isDateHeader;
        public Row(String id, String title, String body, long time, boolean unread) {
            this.id = id; this.title = title; this.body = body;
            this.time = time; this.unread = unread; this.isDateHeader = false;
        }
        private Row(String dateLabel) {
            this.id = ""; this.title = dateLabel; this.body = "";
            this.time = 0; this.unread = false; this.isDateHeader = true;
        }
        public static Row dateHeader(String label) { return new Row(label); }
    }
    public interface OnRowClick { void onClick(Row row); }

    private final List<Row> rows;
    private final OnRowClick listener;

    public NotificationRowAdapter(List<Row> rows, OnRowClick listener) {
        this.rows = rows;
        this.listener = listener;
    }

    @Override public int getItemViewType(int position) {
        return rows.get(position).isDateHeader ? TYPE_DATE : TYPE_ITEM;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_DATE) {
            return new DateHolder(ItemNotificationDateBinding.inflate(inflater, parent, false));
        }
        return new ItemHolder(ItemNotificationBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (holder instanceof DateHolder) {
            ((DateHolder) holder).binding.tvNotifDate.setText(row.title);
            return;
        }
        ItemNotificationBinding binding = ((ItemHolder) holder).binding;
        binding.tvNotifTitle.setText(row.title);
        binding.tvNotifBody.setText(row.body);
        binding.vNotifUnread.setVisibility(row.unread ? View.VISIBLE : View.INVISIBLE);
        binding.getRoot().setOnClickListener(v -> listener.onClick(row));
    }

    @Override public int getItemCount() { return rows.size(); }

    static final class ItemHolder extends RecyclerView.ViewHolder {
        final ItemNotificationBinding binding;
        ItemHolder(ItemNotificationBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }

    static final class DateHolder extends RecyclerView.ViewHolder {
        final ItemNotificationDateBinding binding;
        DateHolder(ItemNotificationDateBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
