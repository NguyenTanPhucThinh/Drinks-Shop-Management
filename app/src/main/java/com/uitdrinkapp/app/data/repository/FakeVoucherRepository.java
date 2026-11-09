package com.uitdrinkapp.app.data.repository;

import android.content.Context;
import com.uitdrinkapp.app.data.mock.VoucherMockData;
import com.uitdrinkapp.app.data.model.SellerVoucher;
import com.uitdrinkapp.app.data.model.Voucher;
import java.util.ArrayList;
import java.util.List;

public final class FakeVoucherRepository implements VoucherRepository {
    private final Context context;

    public FakeVoucherRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override public List<Voucher> getVouchers() {
        List<SellerVoucher> active = SellerVoucherRepositoryProvider.get(context).getActive();
        List<Voucher> result = new ArrayList<>();
        for (SellerVoucher v : active) {
            result.add(new Voucher(
                    v.id,
                    v.code,
                    v.description(),
                    v.expiryDate.isEmpty() ? "Không thời hạn" : "Hạn: " + v.expiryDate
            ));
        }
        if (result.isEmpty()) {
            result.addAll(VoucherMockData.VOUCHERS);
        }
        return result;
    }
}
