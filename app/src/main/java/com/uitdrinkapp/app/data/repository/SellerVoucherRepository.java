package com.uitdrinkapp.app.data.repository;

import com.uitdrinkapp.app.data.model.SellerVoucher;
import java.util.List;

/** Interface for seller voucher management and checkout discount evaluation. */
public interface SellerVoucherRepository {
    List<SellerVoucher> getAll();
    List<SellerVoucher> getActive();
    SellerVoucher getById(String id);
    SellerVoucher save(String id, String code, String type, String value,
            String minimumOrder, String maximumDiscount, String startDate, String expiryDate,
            String terms, boolean active);
    void delete(String id);
    void setActive(String id, boolean active);
    long calculateDiscount(String code, long subtotal);
}
