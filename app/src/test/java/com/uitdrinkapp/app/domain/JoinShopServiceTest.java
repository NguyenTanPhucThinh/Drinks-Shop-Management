package com.uitdrinkapp.app.domain;

import com.uitdrinkapp.app.data.model.ShopSession;
import com.uitdrinkapp.app.data.repository.FakeShopRepository;
import com.uitdrinkapp.app.data.repository.ShopSessionRepository;
import org.junit.Test;
import static org.junit.Assert.*;

public class JoinShopServiceTest {
    private static final class MemorySessions implements ShopSessionRepository {
        ShopSession value;
        public ShopSession getSession() { return value; }
        public void saveSession(ShopSession session) { value = session; }
    }
    @Test public void manualEntryStoresShopAndEnteredTable() {
        MemorySessions sessions = new MemorySessions();
        new JoinShopService(new FakeShopRepository(), sessions).join(" ts-0123 ", "05");
        assertEquals("abc", sessions.getSession().getShopId());
        assertEquals("05", sessions.getSession().getTableNumber());
    }
    @Test public void demoEntryAllowsEmptyOrUnrecognizedCodeWithoutValidation() {
        MemorySessions sessions = new MemorySessions();
        JoinShopService service = new JoinShopService(new FakeShopRepository(), sessions);
        service.join("", "");
        assertEquals("abc", sessions.getSession().getShopId());
        assertEquals("", sessions.getSession().getTableNumber());
        service.join("any-code", "123");
        assertEquals("123", sessions.getSession().getTableNumber());
    }
}
