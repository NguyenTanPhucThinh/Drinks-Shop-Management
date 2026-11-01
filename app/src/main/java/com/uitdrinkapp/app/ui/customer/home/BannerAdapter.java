package com.uitdrinkapp.app.ui.customer.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.uitdrinkapp.app.data.model.Banner;
import com.uitdrinkapp.app.databinding.ItemHomeBannerBinding;
import com.uitdrinkapp.app.ui.common.LocalImages;
import java.util.List;

final class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.Holder> {
    private final List<Banner> banners;
    private final Runnable onClick;
    BannerAdapter(List<Banner> banners, Runnable onClick) { this.banners = banners; this.onClick = onClick; }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemHomeBannerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        Banner banner = banners.get(position % banners.size());
        holder.binding.tvBannerTitle.setText(banner.getTitle());
        holder.binding.tvBannerSubtitle.setText(banner.getSubtitle());
        Glide.with(holder.binding.ivBanner).load(LocalImages.resolve(banner.getImageKey())).centerCrop().into(holder.binding.ivBanner);
        holder.binding.getRoot().setOnClickListener(view -> onClick.run());
    }
    @Override public int getItemCount() { return banners.isEmpty() ? 0 : Integer.MAX_VALUE; }
    static final class Holder extends RecyclerView.ViewHolder {
        final ItemHomeBannerBinding binding;
        Holder(ItemHomeBannerBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
