package com.uitdrinkapp.app.ui.seller.product;

import com.uitdrinkapp.app.data.model.Category;
import com.uitdrinkapp.app.data.model.Product;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public class SellerProductUiPolicyTest {
    private final Product tea = new Product("milk_tea", "Trà sữa trân châu", 35000, "", "", "tea");
    private final Product coffee = new Product("coffee", "Cà phê muối", 39000, "", "", "coffee");
    private final java.util.List<Product> products = Arrays.asList(tea, coffee);
    private final java.util.List<Category> categories = Arrays.asList(
            new Category("tea", "Trà sữa"), new Category("coffee", "Cà phê"));
    @Test public void allListsEverything() {
        assertEquals(2, SellerProductUiPolicy.filter(products,categories,"","").size());
    }
    @Test public void categoryFilter() {
        assertEquals("milk_tea", SellerProductUiPolicy.filter(products,categories,"","tea").get(0).getId());
        assertEquals(1, SellerProductUiPolicy.filter(products,categories,"","tea").size());
    }
    @Test public void queryByProductOrCategory() {
        assertEquals(1, SellerProductUiPolicy.filter(products,categories,"MUỐI","").size());
        assertEquals(1, SellerProductUiPolicy.filter(products,categories,"cà phê","").size());
        assertEquals(0, SellerProductUiPolicy.filter(products,categories,"trà","coffee").size());
    }
    @Test public void missingData() {
        assertTrue(SellerProductUiPolicy.filter(null,categories,"","").isEmpty());
        assertEquals("unknown", SellerProductUiPolicy.categoryName(Collections.emptyList(),"unknown"));
    }
}
