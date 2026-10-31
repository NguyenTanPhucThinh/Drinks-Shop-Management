package com.uitdrinkapp.app.ui.customer.voucher;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.uitdrinkapp.app.data.model.Voucher;
import com.uitdrinkapp.app.databinding.ItemVoucherBinding;
import java.util.List;

final class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.Holder> {
    private final List<Voucher> vouchers;
    VoucherAdapter(List<Voucher> vouchers) { this.vouchers = vouchers; }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(ItemVoucherBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        Voucher voucher = vouchers.get(position);
        holder.binding.tvVoucherTitle.setText(voucher.getTitle());
        holder.binding.tvVoucherDescription.setText(voucher.getDescription());
        holder.binding.tvVoucherExpiry.setText(voucher.getExpiryLabel());
    }
    @Override public int getItemCount() { return vouchers.size(); }
    static final class Holder extends RecyclerView.ViewHolder {
        final ItemVoucherBinding binding;
        Holder(ItemVoucherBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
