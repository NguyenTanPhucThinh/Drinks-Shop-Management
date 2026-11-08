package com.uitdrinkapp.app.ui.seller.product;

import com.uitdrinkapp.app.data.model.SugarLevel;
import com.uitdrinkapp.app.data.model.Temperature;
import java.util.List;

/** Pure Java form validator. Does not update catalog or mutate existing products. */
public final class ProductEditorFormPolicy {
    public enum Issue { NONE, NAME, CATEGORY, PRICE, TEMPERATURE, SUGAR, TOPPING }
    public static final long MAX_PRICE_VND = 999_999_999L;

    public static final class Result {
        public final Issue issue;
        public final String name;
        public final long priceS, priceM, priceL;
        private Result(Issue issue, String name, long s, long m, long l) {
            this.issue = issue; this.name = name; priceS = s; priceM = m; priceL = l;
        }
        public boolean isValid() { return issue == Issue.NONE; }
    }
    private ProductEditorFormPolicy() { }
    public static Result validate(String name, String category, String priceS,
            String priceM, String priceL, List<Temperature> temperatures,
            List<SugarLevel> sugars, boolean allToppings, List<String> toppingIds) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isEmpty()) return invalid(Issue.NAME);
        if (category == null || category.trim().isEmpty()) return invalid(Issue.CATEGORY);
        long s = parseAmount(priceS), m = parseAmount(priceM), l = parseAmount(priceL);
        if (s <= 0L || m <= 0L || l <= 0L) return invalid(Issue.PRICE);
        if (temperatures == null || temperatures.isEmpty()) return invalid(Issue.TEMPERATURE);
        if (sugars == null || sugars.isEmpty()) return invalid(Issue.SUGAR);
        // An empty persisted topping list means ALL. "None" cannot be represented.
        if (!allToppings && (toppingIds == null || toppingIds.isEmpty())) return invalid(Issue.TOPPING);
        return new Result(Issue.NONE, normalized, s, m, l);
    }
    private static Result invalid(Issue issue) { return new Result(issue, "", 0L, 0L, 0L); }
    public static long parseAmount(String text) {
        if (text == null || !text.trim().matches("[0-9]{1,12}")) return -1L;
        try {
            long value = Long.parseLong(text.trim());
            return value <= MAX_PRICE_VND ? value : -1L;
        } catch (NumberFormatException ignored) { return -1L; }
    }
}
