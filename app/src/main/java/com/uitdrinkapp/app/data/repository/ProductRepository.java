package com.uitdrinkapp.app.data.repository;

import com.uitdrinkapp.app.data.model.Category;
import com.uitdrinkapp.app.data.model.Product;
import com.uitdrinkapp.app.data.model.Topping;
import java.util.List;

/** Shared catalog contract. Default CRUD methods protect existing read-only fakes. */
public interface ProductRepository {
    /** Buyer-facing visible products; out-of-stock items may still be shown as disabled. */
    List<Product> getProducts();
    /** Buyer-facing lookup; hidden products are not directly purchasable. */
    Product getProductById(String id);
    List<Category> getCategories();
    /** Buyer-facing list contains available toppings only. */
    List<Topping> getToppings();

    /** Seller-facing queries include hidden and out-of-stock entries. */
    default List<Product> getAllProducts() { return getProducts(); }
    default List<Topping> getAllToppings() { return getToppings(); }
    default Product getProductForSeller(String id) { return getProductById(id); }
    default void saveProduct(Product product) {
        throw new UnsupportedOperationException("Read-only product repository");
    }
    default boolean deleteProduct(String id) {
        throw new UnsupportedOperationException("Read-only product repository");
    }
    default void saveTopping(Topping topping) {
        throw new UnsupportedOperationException("Read-only topping repository");
    }
    default boolean deleteTopping(String id) {
        throw new UnsupportedOperationException("Read-only topping repository");
    }
}
