package com.uitdrinkapp.app.data.repository;
import com.uitdrinkapp.app.data.mock.VoucherMockData;
import com.uitdrinkapp.app.data.model.Voucher;
import java.util.List;
public final class FakeVoucherRepository implements VoucherRepository {
    @Override public List<Voucher> getVouchers() { return VoucherMockData.VOUCHERS; }
}
