package com.uitdrinkapp.app.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.uitdrinkapp.app.data.mock.MockData;
import com.uitdrinkapp.app.data.model.Category;
import com.uitdrinkapp.app.data.model.Product;
import com.uitdrinkapp.app.data.model.Size;
import com.uitdrinkapp.app.data.model.SugarLevel;
import com.uitdrinkapp.app.data.model.Temperature;
import com.uitdrinkapp.app.data.model.Topping;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Persistent local demo catalog: one source is read by Buyer and Seller in this app. */
public final class LocalProductRepository implements ProductRepository {
    private static final String PREFS = "seller_product_catalog_v1";
    private static final String PRODUCTS = "products";
    private static final String TOPPINGS = "toppings";
    private final SharedPreferences preferences;

    public LocalProductRepository(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    @Override public synchronized List<Product> getProducts() {
        List<Product> visible = new ArrayList<>();
        for (Product item : getAllProducts()) if (item.isVisible()) visible.add(item);
        return Collections.unmodifiableList(visible);
    }

    @Override public synchronized List<Product> getAllProducts() {
        if (!preferences.contains(PRODUCTS)) return Collections.unmodifiableList(new ArrayList<>(MockData.PRODUCTS));
        JSONArray data = parse(PRODUCTS);
        List<Product> result = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.optJSONObject(i);
            if (o != null) result.add(productFromJson(o));
        }
        return Collections.unmodifiableList(result);
    }

    @Override public synchronized Product getProductById(String id) {
        Product item = getProductForSeller(id);
        return item != null && item.isVisible() ? item : null;
    }
    @Override public synchronized Product getProductForSeller(String id) {
        if (id == null) return null;
        for (Product p : getAllProducts()) if (id.equals(p.getId())) return p;
        return null;
    }
    @Override public List<Category> getCategories() { return MockData.CATEGORIES; }

    @Override public synchronized List<Topping> getToppings() {
        List<Topping> available = new ArrayList<>();
        for (Topping item : getAllToppings()) if (item.isAvailable()) available.add(item);
        return Collections.unmodifiableList(available);
    }
    @Override public synchronized List<Topping> getAllToppings() {
        if (!preferences.contains(TOPPINGS)) return Collections.unmodifiableList(new ArrayList<>(MockData.TOPPINGS));
        JSONArray data = parse(TOPPINGS);
        List<Topping> result = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.optJSONObject(i);
            if (o != null) result.add(new Topping(o.optString("id"), o.optString("name"),
                    o.optLong("price"), o.optBoolean("available", true)));
        }
        return Collections.unmodifiableList(result);
    }

    @Override public synchronized void saveProduct(Product product) {
        if (product == null || product.getId().trim().isEmpty()
                || product.getName().trim().isEmpty())
            throw new IllegalArgumentException("Product ID and name are required");
        List<Product> items = new ArrayList<>(getAllProducts());
        boolean replaced = false;
        for (int i = 0; i < items.size(); i++) {
            if (product.getId().equals(items.get(i).getId())) {
                items.set(i, product); replaced = true; break;
            }
        }
        if (!replaced) items.add(product);
        saveProducts(items);
    }
    @Override public synchronized boolean deleteProduct(String id) {
        if (id == null) return false;
        List<Product> items = new ArrayList<>(getAllProducts());
        boolean changed = false;
        for (int i = items.size() - 1; i >= 0; i--) {
            if (id.equals(items.get(i).getId())) { items.remove(i); changed = true; }
        }
        if (changed) saveProducts(items);
        return changed;
    }
    @Override public synchronized void saveTopping(Topping topping) {
        if (topping == null || topping.getId().trim().isEmpty()
                || topping.getName().trim().isEmpty())
            throw new IllegalArgumentException("Topping ID and name are required");
        List<Topping> items = new ArrayList<>(getAllToppings());
        boolean replaced = false;
        for (int i = 0; i < items.size(); i++) {
            if (topping.getId().equals(items.get(i).getId())) {
                items.set(i, topping); replaced = true; break;
            }
        }
        if (!replaced) items.add(topping);
        saveToppings(items);
    }
    @Override public synchronized boolean deleteTopping(String id) {
        if (id == null) return false;
        List<Topping> items = new ArrayList<>(getAllToppings());
        boolean changed = false;
        for (int i = items.size() - 1; i >= 0; i--) {
            if (id.equals(items.get(i).getId())) { items.remove(i); changed = true; }
        }
        if (changed) saveToppings(items);
        return changed;
    }

    private JSONArray parse(String key) {
        String raw = preferences.getString(key, "[]");
        try { return new JSONArray(raw); }
        catch (JSONException ex) {
            // Do not silently overwrite corrupted customer/seller data with new seed data.
            throw new IllegalStateException("Invalid stored catalog: " + key, ex);
        }
    }
    private void saveProducts(List<Product> products) {
        JSONArray array = new JSONArray();
        try { for (Product item : products) array.put(productToJson(item)); }
        catch (JSONException ex) { throw new IllegalStateException("Cannot save products", ex); }
        if (!preferences.edit().putString(PRODUCTS, array.toString()).commit())
            throw new IllegalStateException("Cannot persist products");
    }
    private void saveToppings(List<Topping> toppings) {
        JSONArray array = new JSONArray();
        try {
            for (Topping item : toppings) {
                JSONObject row = new JSONObject();
                row.put("id", item.getId()); row.put("name", item.getName());
                row.put("price", item.getPrice()); row.put("available", item.isAvailable());
                array.put(row);
            }
        } catch (JSONException ex) { throw new IllegalStateException("Cannot save toppings", ex); }
        if (!preferences.edit().putString(TOPPINGS, array.toString()).commit())
            throw new IllegalStateException("Cannot persist toppings");
    }
    private static JSONObject productToJson(Product item) throws JSONException {
        JSONObject row = new JSONObject();
        row.put("id", item.getId()); row.put("name", item.getName());
        row.put("price", item.getPrice()); row.put("imageUrl", item.getImageUrl());
        row.put("description", item.getDescription()); row.put("categoryId", item.getCategoryId());
        row.put("available", item.isAvailable()); row.put("visible", item.isVisible());
        JSONObject sizes = new JSONObject();
        for (Map.Entry<Size, Long> entry : item.getSizePrices().entrySet())
            sizes.put(entry.getKey().name(), entry.getValue());
        row.put("sizePrices", sizes);
        JSONArray temperatures = new JSONArray();
        for (Temperature t : item.getTemperatures()) temperatures.put(t.name());
        row.put("temperatures", temperatures);
        JSONArray sugarLevels = new JSONArray();
        for (SugarLevel s : item.getSugarLevels()) sugarLevels.put(s.name());
        row.put("sugarLevels", sugarLevels);
        row.put("toppingIds", new JSONArray(item.getToppingIds()));
        return row;
    }
    private static Product productFromJson(JSONObject row) {
        Map<Size, Long> prices = new EnumMap<>(Size.class);
        JSONObject saved = row.optJSONObject("sizePrices");
        if (saved != null) {
            for (Size size : Size.values()) {
                if (saved.has(size.name()) && saved.optLong(size.name(), -1L) >= 0L)
                    prices.put(size, saved.optLong(size.name()));
            }
        }
        List<Temperature> temperatures = enumList(row.optJSONArray("temperatures"), Temperature.class);
        List<SugarLevel> sugars = enumList(row.optJSONArray("sugarLevels"), SugarLevel.class);
        List<String> toppingIds = new ArrayList<>();
        JSONArray ids = row.optJSONArray("toppingIds");
        if (ids != null) for (int i = 0; i < ids.length(); i++) toppingIds.add(ids.optString(i));
        return new Product(row.optString("id"), row.optString("name"), row.optLong("price"),
                row.optString("imageUrl"), row.optString("description"),
                migrateCategory(row.optString("categoryId"), row.optString("name")),
                prices, temperatures, sugars, toppingIds,
                row.optBoolean("available", row.optBoolean("inStock", true)),
                row.optBoolean("visible", true));
    }
    /** Danh mục cũ "tea" (Trà & trà sữa) đã tách thành Trà sữa / Trà trái cây. */
    private static String migrateCategory(String categoryId, String name) {
        if (!"tea".equals(categoryId)) return categoryId;
        String n = name == null ? "" : name.toLowerCase(java.util.Locale.ROOT);
        boolean fruit = n.contains("trà đào") || n.contains("trà trái") || n.contains("trà vải")
                || n.contains("trà cam") || n.contains("trà chanh") || n.contains("trà dâu");
        return fruit ? "fruit_tea" : "milk_tea";
    }
    private static <T extends Enum<T>> List<T> enumList(JSONArray values, Class<T> type) {
        if (values == null) return null; // legacy: all supported
        List<T> result = new ArrayList<>();
        for (int i = 0; i < values.length(); i++) {
            try { result.add(Enum.valueOf(type, values.optString(i))); }
            catch (IllegalArgumentException ignored) { /* ignore retired enum values */ }
        }
        return result;
    }
}
