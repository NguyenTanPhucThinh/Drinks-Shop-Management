package com.uitdrinkapp.app.ui.seller.product;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public final class SellerProductFormFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new FrameLayout(requireContext());
    }
}
