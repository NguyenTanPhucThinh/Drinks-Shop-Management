package com.uitdrinkapp.app.ui.customer.notification;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.uitdrinkapp.app.databinding.ItemNotificationBinding;
import java.util.List;

public final class NotificationRowAdapter extends RecyclerView.Adapter<NotificationRowAdapter.Holder> {
    public static final class Row {
        public final String id, title, body;
        public final long time;
        public final boolean unread;
        public Row(String id, String title, String body, long time, boolean unread) {
            this.id = id; this.title = title; this.body = body;
            this.time = time; this.unread = unread;
        }
    }
    public interface OnRowClick { void onClick(Row row); }

    private final List<Row> rows;
    private final OnRowClick listener;

    public NotificationRowAdapter(List<Row> rows, OnRowClick listener) {
        this.rows = rows;
        this.listener = listener;
    }

    public void replace(List<Row> value) {
        rows.clear();
        if (value != null) rows.addAll(value);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Row row = rows.get(position);
        holder.binding.tvNotifTitle.setText(row.title);
        holder.binding.tvNotifBody.setText(row.body);
        holder.binding.tvNotifTime.setText(DateUtils.getRelativeTimeSpanString(
                row.time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
        holder.binding.vNotifUnread.setVisibility(row.unread ? View.VISIBLE : View.INVISIBLE);
        holder.binding.getRoot().setOnClickListener(v -> listener.onClick(row));
    }

    @Override public int getItemCount() { return rows.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemNotificationBinding binding;
        Holder(ItemNotificationBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
