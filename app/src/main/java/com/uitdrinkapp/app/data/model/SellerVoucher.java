package com.uitdrinkapp.app.data.model;

/** Editable seller-side voucher used by the local management preview. */
public final class SellerVoucher {
    public final String id;
    public String code;
    public String type;
    public String value;
    public String minimumOrder;
    public String maximumDiscount;
    public String startDate;
    public String expiryDate;
    public String terms;
    public boolean active;
    public boolean expired;

    public SellerVoucher(String id, String code, String type, String value,
            String minimumOrder, String maximumDiscount, String startDate, String expiryDate,
            String terms, boolean active, boolean expired) {
        this.id = id;
        this.code = code;
        this.type = type;
        this.value = value;
        this.minimumOrder = minimumOrder;
        this.maximumDiscount = maximumDiscount;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.terms = terms;
        this.active = active;
        this.expired = expired;
    }

    public String description() {
        String result;
        if ("Tặng topping".equals(type)) result = "Tặng topping miễn phí";
        else if ("Giảm %".equals(type)) result = "Giảm " + value + "% tối đa " + maximumDiscount + "đ";
        else result = "Giảm " + value + "đ" + (minimumOrder.isEmpty() ? "" : " · Đơn từ " + minimumOrder + "đ");
        return terms.isEmpty() ? result : result + " · " + terms;
    }
}
