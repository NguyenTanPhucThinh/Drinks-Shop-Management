package com.uitdrinkapp.app.data.mock;

import com.uitdrinkapp.app.data.model.Category;
import com.uitdrinkapp.app.data.model.Product;
import com.uitdrinkapp.app.data.model.Topping;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Shared product catalog mock used by the product repositories. */
public final class MockData {
    private MockData() { }

    public static final List<Category> CATEGORIES = Collections.unmodifiableList(Arrays.asList(
            new Category("tea", "Trà & trà sữa"), new Category("coffee", "Cà phê")));
    public static final List<Topping> TOPPINGS = Collections.unmodifiableList(Arrays.asList(
            new Topping("black_pearl", "Trân châu đen", 5000L),
            new Topping("egg_pudding", "Pudding trứng", 7000L)));
    public static final List<Product> PRODUCTS = Collections.unmodifiableList(Arrays.asList(
            new Product("milk_tea", "Trà sữa trân châu", 35000L, "home_milk_tea", "Trà sữa thơm béo cùng trân châu dai mềm.", "tea"),
            new Product("matcha", "Matcha đá xay", 39000L, "home_matcha", "Matcha xanh mát, vị trà thanh dịu.", "tea"),
            new Product("peach_tea", "Trà đào cam sả", 42000L, "home_peach_tea", "Trà đào tươi mát với cam và sả.", "tea"),
            new Product("salt_coffee", "Cà phê muối", 39000L, "home_salt_coffee", "Cà phê đậm đà phủ kem muối.", "coffee"),
            new Product("classic_tea", "Trà sữa truyền thống", 30000L, "home_milk_tea", "Trà sữa nguyên bản.", "tea")));
}
