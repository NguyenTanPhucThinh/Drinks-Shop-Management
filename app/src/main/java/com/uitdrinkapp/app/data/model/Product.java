package com.uitdrinkapp.app.data.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Immutable catalog entry shared by buyer/seller. Prices are in VND (long). */
public final class Product {
    private final String id;
    private final String name;
    /** Historical alias for the size-S price; retained for existing callers. */
    private final long price;
    private final String imageUrl;
    private final String description;
    private final String categoryId;
    private final Map<Size, Long> sizePrices;
    private final List<Temperature> temperatures;
    private final List<SugarLevel> sugarLevels;
    /** Empty means that all available toppings are allowed (legacy behavior). */
    private final List<String> toppingIds;
    /** Inventory availability; separate from seller visibility. */
    private final boolean inStock;
    private final boolean visible;

    /** Legacy constructor: default size surcharges, both temperatures and all sugar levels. */
    public Product(String id, String name, long price, String imageUrl,
                   String description, String categoryId) {
        this(id, name, price, imageUrl, description, categoryId,
                Collections.emptyMap(), null, null, null, true, true);
    }

    /** PR #12 constructor retained exactly; explicit prices for all sizes. */
    public Product(String id, String name, long price, String imageUrl, String description,
                   String categoryId, long priceS, long priceM, long priceL, boolean inStock) {
        this(id, name, priceS, imageUrl, description, categoryId,
                prices(priceS, priceM, priceL), null, null, null, inStock, true);
    }

    /** New seller-editable constructor; missing sizes fall back to legacy surcharges. */
    public Product(String id, String name, long price, String imageUrl, String description,
                   String categoryId, Map<Size, Long> sizePrices,
                   List<Temperature> temperatures, List<SugarLevel> sugarLevels,
                   List<String> toppingIds, boolean available, boolean visible) {
        this.id = id == null ? "" : id;
        this.name = name == null ? "" : name;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
        this.description = description == null ? "" : description;
        this.categoryId = categoryId == null ? "" : categoryId;
        EnumMap<Size, Long> safePrices = new EnumMap<>(Size.class);
        if (sizePrices != null) {
            for (Map.Entry<Size, Long> e : sizePrices.entrySet()) {
                if (e.getKey() != null && e.getValue() != null && e.getValue() >= 0L)
                    safePrices.put(e.getKey(), e.getValue());
            }
        }
        this.sizePrices = Collections.unmodifiableMap(safePrices);
        // Keep the single legacy price synchronized with the S price.
        this.price = safePrices.containsKey(Size.S) ? safePrices.get(Size.S) : Math.max(0L, price);
        this.temperatures = copyOrDefault(temperatures, Arrays.asList(Temperature.values()));
        this.sugarLevels = copyOrDefault(sugarLevels, Arrays.asList(SugarLevel.values()));
        this.toppingIds = toppingIds == null
                ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(toppingIds));
        this.inStock = available;
        this.visible = visible;
    }

    private static <T> List<T> copyOrDefault(List<T> value, List<T> fallback) {
        return Collections.unmodifiableList(new ArrayList<>(value == null ? fallback : value));
    }
    private static Map<Size, Long> prices(long s, long m, long l) {
        EnumMap<Size, Long> result = new EnumMap<>(Size.class);
        result.put(Size.S, Math.max(0L, s));
        result.put(Size.M, Math.max(0L, m));
        result.put(Size.L, Math.max(0L, l));
        return result;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public long getPriceS() { return getPriceFor(Size.S); }
    public long getPriceM() { return getPriceFor(Size.M); }
    public long getPriceL() { return getPriceFor(Size.L); }
    public long getPriceForSize(Size size) { return getPriceFor(size); }
    public long getPriceFor(Size size) {
        Size selected = size == null ? Size.S : size;
        Long exact = sizePrices.get(selected);
        return exact != null ? exact : price + selected.getExtraPrice();
    }
    public Map<Size, Long> getSizePrices() { return sizePrices; }
    public List<Temperature> getTemperatures() { return temperatures; }
    public List<SugarLevel> getSugarLevels() { return sugarLevels; }
    public List<String> getToppingIds() { return toppingIds; }
    public boolean isInStock() { return inStock; }
    public boolean isAvailable() { return inStock; }
    public boolean isVisible() { return visible; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public String getCategoryId() { return categoryId; }
}
