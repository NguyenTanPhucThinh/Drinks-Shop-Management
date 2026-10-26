package com.uitdrinkapp.app.data.repository;
import com.uitdrinkapp.app.data.mock.HomeMockData;
import com.uitdrinkapp.app.data.model.Banner;
import java.util.List;
public final class FakeHomeRepository implements HomeRepository {
    public List<String> getFeaturedProductIds() { return HomeMockData.FEATURED_IDS; }
    public List<String> getRecommendedProductIds() { return HomeMockData.RECOMMENDED_IDS; }
    public List<Banner> getBanners() { return HomeMockData.BANNERS; }
}
