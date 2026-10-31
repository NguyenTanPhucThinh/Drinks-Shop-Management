package com.uitdrinkapp.app.data.repository;
import com.uitdrinkapp.app.data.model.*;

public interface ShopSessionRepository {
    ShopSession getSession();
    void saveSession(ShopSession session);
}
