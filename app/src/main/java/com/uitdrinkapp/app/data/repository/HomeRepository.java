package com.uitdrinkapp.app.data.repository;
import com.uitdrinkapp.app.data.model.*;
import java.util.List;

public interface HomeRepository {
    List<String> getFeaturedProductIds();
    List<String> getRecommendedProductIds();
    List<Banner> getBanners();
}
