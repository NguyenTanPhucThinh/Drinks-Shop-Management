package com.uitdrinkapp.app.domain;
import org.junit.Test;
import static org.junit.Assert.*;

public class ShopQrCodeTest {
    @Test public void plainShopCodeKeepsTableOptional() {
        ShopQrCode code = ShopQrCode.parse(" ts-0123 ");
        assertEquals("TS-0123", code.getShopCode());
        assertNull(code.getTableNumber());
    }
    @Test public void shopLinkCarriesTableAndDecodesValues() {
        ShopQrCode code = ShopQrCode.parse("trasua://join?code=ts%2D0123&table=05");
        assertEquals("TS-0123", code.getShopCode());
        assertEquals("05", code.getTableNumber());
    }
    @Test public void unrelatedAndMalformedLinksAreNotFollowed() {
        assertNull(ShopQrCode.parse("https://example.com"));
        assertNull(ShopQrCode.parse("trasua://join?table=05"));
        assertNull(ShopQrCode.parse("trasua://join?code=%GG"));
        assertNull(ShopQrCode.parse(null));
    }
}
