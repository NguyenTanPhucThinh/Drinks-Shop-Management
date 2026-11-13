package com.uitdrinkapp.app.ui.customer.shopinfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.uitdrinkapp.app.data.model.Shop;
import com.uitdrinkapp.app.data.model.ShopSession;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentShopInfoBinding;

/** Chi tiết quán cho người mua; dữ liệu đọc từ ShopRepository (SharedPreferences). */
public final class ShopInfoFragment extends Fragment {
    private FragmentShopInfoBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentShopInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnShopInfoBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());
    }

    @Override
    public void onResume() {
        super.onResume();
        bindShop();
    }

    private void bindShop() {
        RepositoryProvider repositories = RepositoryProvider.get(requireContext());
        ShopSession session = repositories.sessions().getSession();
        Shop shop = session == null
                ? repositories.shops().findByCode(repositories.shops().getDemoCode())
                : repositories.shops().findById(session.getShopId());
        if (shop == null) return;
        binding.tvShopInfoName.setText(shop.getName());
        binding.tvShopInfoDescription.setText(shop.getDescription());
        binding.tvShopInfoHours.setText(shop.getHours().replace(" - ", " – "));
        binding.tvShopInfoAddress.setText(shop.getAddress());
        binding.tvShopInfoPhone.setText(shop.getPhone());
        binding.tvShopInfoEmail.setText(shop.getEmail());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
