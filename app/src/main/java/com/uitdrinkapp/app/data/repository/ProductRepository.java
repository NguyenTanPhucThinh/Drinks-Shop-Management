package com.uitdrinkapp.app.data.repository;
import com.uitdrinkapp.app.data.model.*;
import java.util.List;

public interface ProductRepository {
    List<Product> getProducts();
    Product getProductById(String id);
    List<Category> getCategories();
    List<Topping> getToppings();
}
