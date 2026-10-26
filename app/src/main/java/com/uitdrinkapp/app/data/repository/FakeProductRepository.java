package com.uitdrinkapp.app.data.repository;
import com.uitdrinkapp.app.data.mock.MockData;
import com.uitdrinkapp.app.data.model.*;
import java.util.List;
public final class FakeProductRepository implements ProductRepository {
    public List<Product> getProducts() { return MockData.PRODUCTS; }
    public Product getProductById(String id) {
        for (Product product : getProducts()) if (product.getId().equals(id)) return product;
        return null;
    }
    public List<Topping> getToppings() { return MockData.TOPPINGS; }
    public List<Category> getCategories() { return MockData.CATEGORIES; }
}
