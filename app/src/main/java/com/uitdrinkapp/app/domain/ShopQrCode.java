package com.uitdrinkapp.app.domain;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Local QR contract: a shop code, or trasua://join?code=TS-0123&table=05. */
public final class ShopQrCode {
    private final String shopCode;
    private final String tableNumber;
    private ShopQrCode(String shopCode, String tableNumber) { this.shopCode = shopCode; this.tableNumber = tableNumber; }
    public String getShopCode() { return shopCode; }
    public String getTableNumber() { return tableNumber; }
    public static ShopQrCode parse(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        String value = raw.trim();
        if (!value.contains("://")) return new ShopQrCode(value.toUpperCase(Locale.ROOT), null);
        try {
            URI uri = URI.create(value);
            if (!"trasua".equalsIgnoreCase(uri.getScheme()) || !"join".equalsIgnoreCase(uri.getHost())) return null;
            String code = null, table = null;
            if (uri.getRawQuery() == null) return null;
            for (String part : uri.getRawQuery().split("&")) {
                String[] pair = part.split("=", 2);
                if (pair.length != 2) continue;
                String decoded = URLDecoder.decode(pair[1], StandardCharsets.UTF_8.name()).trim();
                if ("code".equals(pair[0])) code = decoded.toUpperCase(Locale.ROOT);
                if ("table".equals(pair[0])) table = decoded;
            }
            return code == null || code.isEmpty() ? null : new ShopQrCode(code, table);
        } catch (IllegalArgumentException | java.io.UnsupportedEncodingException exception) {
            return null;
        }
    }
}
