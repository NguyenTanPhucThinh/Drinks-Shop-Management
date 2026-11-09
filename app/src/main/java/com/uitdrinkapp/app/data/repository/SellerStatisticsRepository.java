package com.uitdrinkapp.app.data.repository;

import com.uitdrinkapp.app.data.model.Order;
import com.uitdrinkapp.app.data.model.OrderItem;
import com.uitdrinkapp.app.data.model.OrderStatus;
import com.uitdrinkapp.app.data.model.Review;
import com.uitdrinkapp.app.data.order.BuyerOrderRepository;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Builds seller reports from the same fake orders and reviews used by buyer history/review. */
public final class SellerStatisticsRepository {
    private final BuyerOrderRepository orders;

    public SellerStatisticsRepository(BuyerOrderRepository orders) {
        this.orders = orders;
    }

    public Report getReport(int period) {
        Calendar now = Calendar.getInstance();
        Calendar start = (Calendar) now.clone();
        Calendar previousStart = (Calendar) now.clone();
        int bucketCount;
        if (period == 0) {
            int daysSinceMonday = (now.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            start.add(Calendar.DAY_OF_YEAR, -daysSinceMonday);
            previousStart.add(Calendar.DAY_OF_YEAR, -daysSinceMonday - 7);
            bucketCount = 7;
        } else if (period == 1) {
            start.set(Calendar.DAY_OF_MONTH, 1);
            previousStart.set(Calendar.DAY_OF_MONTH, 1);
            previousStart.add(Calendar.MONTH, -1);
            bucketCount = 5;
        } else {
            start.set(Calendar.DAY_OF_YEAR, 1);
            previousStart.set(Calendar.DAY_OF_YEAR, 1);
            previousStart.add(Calendar.YEAR, -1);
            bucketCount = 12;
        }
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        previousStart.set(Calendar.HOUR_OF_DAY, 0);
        previousStart.set(Calendar.MINUTE, 0);
        previousStart.set(Calendar.SECOND, 0);
        previousStart.set(Calendar.MILLISECOND, 0);

        long[] bucketRevenue = new long[bucketCount];
        Map<String, ProductSales> products = new LinkedHashMap<>();
        Map<String, ProductRating> ratings = new LinkedHashMap<>();
        Map<String, CustomerSales> customers = new LinkedHashMap<>();
        long revenue = 0;
        long previousRevenue = 0;

        for (Order order : orders.getOrders()) {
            if (order.status != OrderStatus.COMPLETED) continue;
            if (order.createdAt >= previousStart.getTimeInMillis()
                    && order.createdAt < start.getTimeInMillis()) {
                previousRevenue += order.totalAmount();
            }
            if (order.createdAt < start.getTimeInMillis()
                    || order.createdAt > now.getTimeInMillis()) continue;
            revenue += order.totalAmount();
            int bucket = bucketFor(order.createdAt, period, start, bucketCount);
            if (bucket >= 0 && bucket < bucketCount) {
                bucketRevenue[bucket] += order.totalAmount();
            }

            for (OrderItem item : order.items) {
                String key = item.productId.isEmpty() ? item.productName : item.productId;
                ProductSales sales = products.get(key);
                if (sales == null) {
                    sales = new ProductSales(item.productId, item.productName);
                    products.put(key, sales);
                }
                sales.quantity += item.quantity;
            }

            if (!order.customerName.trim().isEmpty()) {
                CustomerSales customer = customers.get(order.customerName);
                if (customer == null) {
                    customer = new CustomerSales(order.customerName);
                    customers.put(order.customerName, customer);
                }
                customer.orderCount++;
                customer.spent += order.totalAmount();
            }

            Review review = orders.getReview(order.orderId);
            if (review == null) continue;
            String productKey = review.productId.isEmpty() ? review.productName : review.productId;
            ProductRating rating = ratings.get(productKey);
            if (rating == null) {
                rating = new ProductRating(review.productId, review.productName);
                ratings.put(productKey, rating);
            }
            rating.count++;
            rating.ratingTotal += review.rating;
        }

        long max = 0;
        for (long value : bucketRevenue) max = Math.max(max, value);
        List<ChartPoint> chart = new ArrayList<>();
        Calendar cursor = (Calendar) start.clone();
        for (int i = 0; i < bucketCount; i++) {
            String label;
            if (period == 0) {
                int day = cursor.get(Calendar.DAY_OF_WEEK);
                label = new String[] {"CN", "T2", "T3", "T4", "T5", "T6", "T7"}[day - 1];
                cursor.add(Calendar.DAY_OF_YEAR, 1);
            } else if (period == 1) {
                label = String.valueOf(1 + i * 7);
            } else {
                label = "T" + (i + 1);
            }
            int height = max == 0 ? 0 : Math.max(12, Math.round(bucketRevenue[i] * 100f / max));
            chart.add(new ChartPoint(label, height));
        }

        List<ProductSales> selling = new ArrayList<>(products.values());
        selling.sort((a, b) -> Integer.compare(b.quantity, a.quantity));
        List<ProductRating> rated = new ArrayList<>(ratings.values());
        rated.sort((a, b) -> Float.compare(b.average(), a.average()));
        List<CustomerSales> topCustomers = new ArrayList<>(customers.values());
        topCustomers.sort((a, b) -> Long.compare(b.spent, a.spent));
        return new Report(revenue, previousRevenue, chart, selling, rated, topCustomers);
    }

    private int bucketFor(long timestamp, int period, Calendar start, int bucketCount) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp);
        if (period == 0) {
            return (int) ((date.getTimeInMillis() - start.getTimeInMillis()) / 86_400_000L);
        }
        if (period == 1) return (date.get(Calendar.DAY_OF_MONTH) - 1) / 7;
        int months = (date.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12
                + date.get(Calendar.MONTH) - start.get(Calendar.MONTH);
        return Math.max(0, Math.min(bucketCount - 1, months));
    }

    public static final class Report {
        public final long revenue;
        public final long previousRevenue;
        public final List<ChartPoint> chart;
        public final List<ProductSales> selling;
        public final List<ProductRating> rated;
        public final List<CustomerSales> customers;
        Report(long revenue, long previousRevenue, List<ChartPoint> chart, List<ProductSales> selling,
                List<ProductRating> rated, List<CustomerSales> customers) {
            this.revenue = revenue;
            this.previousRevenue = previousRevenue;
            this.chart = Collections.unmodifiableList(chart);
            this.selling = Collections.unmodifiableList(selling);
            this.rated = Collections.unmodifiableList(rated);
            this.customers = Collections.unmodifiableList(customers);
        }
    }

    public static final class ChartPoint {
        public final String label;
        public final int heightPercent;
        ChartPoint(String label, int heightPercent) {
            this.label = label;
            this.heightPercent = heightPercent;
        }
    }

    public static final class ProductSales {
        public final String productId;
        public final String name;
        public int quantity;
        ProductSales(String productId, String name) {
            this.productId = productId;
            this.name = name;
        }
    }

    public static final class ProductRating {
        public final String productId;
        public final String name;
        public int count;
        public int ratingTotal;
        ProductRating(String productId, String name) {
            this.productId = productId;
            this.name = name;
        }
        public float average() { return count == 0 ? 0 : (float) ratingTotal / count; }
    }

    public static final class CustomerSales {
        public final String name;
        public int orderCount;
        public long spent;
        CustomerSales(String name) { this.name = name; }
    }
}
