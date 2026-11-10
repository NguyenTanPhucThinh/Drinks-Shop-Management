package com.uitdrinkapp.app.ui.seller.notification;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.Notification;
import com.uitdrinkapp.app.databinding.ItemSellerNotificationBinding;

import java.util.ArrayList;
import java.util.List;

public final class SellerNotificationAdapter extends RecyclerView.Adapter<SellerNotificationAdapter.Holder> {

    public static final class Row {
        public final String id;
        public final String title;
        public final String body;
        public final long time;
        public final boolean unread;
        public final Notification.Category category;
        public final Notification.Type type;

        public Row(String id, String title, String body, long time, boolean unread,
                   Notification.Category category, Notification.Type type) {
            this.id = id;
            this.title = title;
            this.body = body;
            this.time = time;
            this.unread = unread;
            this.category = category;
            this.type = type;
        }
    }

    public interface OnRowClick {
        void onClick(Row row);
    }

    private final List<Row> allRows = new ArrayList<>();
    private final List<Row> displayedRows = new ArrayList<>();
    private final OnRowClick listener;
    private int currentFilter = 0; // 0: All, 1: Orders, 2: System

    public SellerNotificationAdapter(List<Row> rows, OnRowClick listener) {
        if (rows != null) {
            allRows.addAll(rows);
        }
        this.listener = listener;
        filter(0);
    }

    public void replace(List<Row> value) {
        allRows.clear();
        if (value != null) {
            allRows.addAll(value);
        }
        filter(currentFilter);
    }

    public void filter(int filterIndex) {
        currentFilter = filterIndex;
        displayedRows.clear();
        for (Row row : allRows) {
            if (filterIndex == 1 && row.category != Notification.Category.ORDER) continue;
            if (filterIndex == 2 && row.category != Notification.Category.SYSTEM) continue;
            displayedRows.add(row);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemSellerNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Row row = displayedRows.get(position);
        holder.binding.tvNotifTitle.setText(row.title);
        holder.binding.tvNotifBody.setText(row.body);
        holder.binding.tvNotifTime.setText(DateUtils.getRelativeTimeSpanString(
                row.time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
        holder.binding.vNotifUnread.setVisibility(row.unread ? View.VISIBLE : View.GONE);

        // Set icon based on type/category
        if (row.category == Notification.Category.ORDER) {
            holder.binding.ivNotifIcon.setImageResource(R.drawable.ic_seller_orders);
        } else if (row.type == Notification.Type.REVIEW_RECEIVED) {
            holder.binding.ivNotifIcon.setImageResource(R.drawable.ic_heart);
        } else if (row.type == Notification.Type.VOUCHER_USAGE_LIMIT) {
            holder.binding.ivNotifIcon.setImageResource(R.drawable.ic_gift);
        } else {
            holder.binding.ivNotifIcon.setImageResource(R.drawable.ic_seller_statistic);
        }

        holder.binding.getRoot().setOnClickListener(v -> listener.onClick(row));
    }

    @Override
    public int getItemCount() {
        return displayedRows.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemSellerNotificationBinding binding;

        Holder(ItemSellerNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
