package com.uitdrinkapp.app.data.repository;
import com.uitdrinkapp.app.data.model.*;

public interface ShopRepository {
    Shop findByCode(String code);
    Shop findById(String id);
    String getDemoCode();
    String getDemoTable();
}
