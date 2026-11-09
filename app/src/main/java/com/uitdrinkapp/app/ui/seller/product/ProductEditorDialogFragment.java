package com.uitdrinkapp.app.ui.seller.product;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.bumptech.glide.Glide;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.Category;
import com.uitdrinkapp.app.data.model.Product;
import com.uitdrinkapp.app.data.model.Size;
import com.uitdrinkapp.app.data.model.SugarLevel;
import com.uitdrinkapp.app.data.model.Temperature;
import com.uitdrinkapp.app.data.model.Topping;
import com.uitdrinkapp.app.data.repository.ProductRepository;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentThienProductEditorBinding;
import com.uitdrinkapp.app.ui.common.LocalImages;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** GD07 full-screen editor launched from Seller screen 17; does not modify shared navigation. */
public final class ProductEditorDialogFragment extends DialogFragment {
    public static final String TAG = "thien_product_editor";
    public static final String RESULT_KEY = "thien_product_saved";
    private static final String ARG_ID = "product_id";
    private static final String STATE_IMAGE = "editor_image";
    private static final String STATE_CATEGORY = "editor_category";
    private static final String STATE_TEMPS = "editor_temps";
    private static final String STATE_SUGARS = "editor_sugars";
    private static final String STATE_TOPPINGS = "editor_toppings";
    private static final String STATE_ALL = "editor_all_toppings";
    private static final String[] IMAGE_KEYS = {
            "home_milk_tea", "home_matcha", "home_peach_tea", "home_salt_coffee"};
    private FragmentThienProductEditorBinding binding;
    private ProductRepository catalog;
    private Product original;
    private String imageKey;
    private List<Category> categories;
    private final List<ToppingChoice> toppings = new ArrayList<>();
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        requireContext().getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception ignored) {}
                    imageKey = uri.toString();
                    updateImage();
                }
            }
    );

    public static ProductEditorDialogFragment create(@Nullable String productId) {
        ProductEditorDialogFragment editor = new ProductEditorDialogFragment();
        Bundle args = new Bundle();
        if (productId != null) args.putString(ARG_ID, productId);
        editor.setArguments(args);
        return editor;
    }
    @Override public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        setStyle(STYLE_NO_TITLE, 0);
    }
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup parent, @Nullable Bundle state) {
        binding = FragmentThienProductEditorBinding.inflate(inflater, parent, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        catalog = RepositoryProvider.get(requireContext()).products();
        String id = getArguments() == null ? null : getArguments().getString(ARG_ID);
        original = id == null ? null : catalog.getProductForSeller(id);
        if (id != null && original == null) {
            new AlertDialog.Builder(requireContext())
                    .setMessage(R.string.thien_editor_missing)
                    .setPositiveButton(android.R.string.ok, (d, w) -> dismiss()).show();
            return;
        }
        binding.tvThienEditorTitle.setText(original == null
                ? R.string.thien_editor_title_new : R.string.thien_editor_title_edit);
        binding.btnThienEditorDelete.setVisibility(original == null ? View.GONE : View.VISIBLE);
        imageKey = original == null ? IMAGE_KEYS[0] : original.getImageUrl();
        if (state != null) imageKey = state.getString(STATE_IMAGE, imageKey);
        updateImage();
        binding.btnThienEditorPhoto.setOnClickListener(clicked -> chooseImageSource());
        binding.btnThienEditorBack.setOnClickListener(clicked -> dismiss());
        binding.btnThienEditorCancel.setOnClickListener(clicked -> dismiss());
        binding.btnThienEditorSave.setOnClickListener(clicked -> save());
        binding.btnThienEditorDelete.setOnClickListener(clicked -> confirmDelete());
        binding.btnThienEditorAddTopping.setOnClickListener(clicked -> addTopping());
        binding.etThienEditorName.setText(original == null ? "" : original.getName());
        binding.etThienEditorDescription.setText(original == null ? "" : original.getDescription());
        binding.etThienEditorPriceS.setText(original == null ? "" : String.valueOf(original.getPriceFor(Size.S)));
        binding.etThienEditorPriceM.setText(original == null ? "" : String.valueOf(original.getPriceFor(Size.M)));
        binding.etThienEditorPriceL.setText(original == null ? "" : String.valueOf(original.getPriceFor(Size.L)));
        binding.swThienEditorInStock.setChecked(original == null || original.isAvailable());
        binding.swThienEditorVisible.setChecked(original == null || original.isVisible());
        setupCategories(state);
        setupOptions(state);
        setupToppings(state);
    }
    @Override public void onStart() {
        super.onStart();
        if (getDialog() == null || getDialog().getWindow() == null) return;
        Window w = getDialog().getWindow();
        w.setBackgroundDrawableResource(R.color.seller_background);
        w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }
    private void setupCategories(@Nullable Bundle state) {
        categories = catalog.getCategories();
        List<String> names = new ArrayList<>();
        for (Category c : categories) names.add(c.getName());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spThienEditorCategory.setAdapter(adapter);
        String selected = state != null ? state.getString(STATE_CATEGORY) : null;
        if (selected == null && original != null) selected = original.getCategoryId();
        if (selected != null) for (int i = 0; i < categories.size(); i++)
            if (selected.equals(categories.get(i).getId())) { binding.spThienEditorCategory.setSelection(i); break; }
    }
    private void setupOptions(@Nullable Bundle state) {
        List<Temperature> temps = original == null ? Arrays.asList(Temperature.values()) : original.getTemperatures();
        List<SugarLevel> sugars = original == null ? Arrays.asList(SugarLevel.values()) : original.getSugarLevels();
        if (state != null && state.containsKey(STATE_TEMPS)) {
            ArrayList<String> saved = state.getStringArrayList(STATE_TEMPS);
            temps = enumValues(saved, Temperature.class);
        }
        if (state != null && state.containsKey(STATE_SUGARS)) {
            ArrayList<String> saved = state.getStringArrayList(STATE_SUGARS);
            sugars = enumValues(saved, SugarLevel.class);
        }
        binding.chipThienEditorHot.setChecked(temps.contains(Temperature.HOT));
        binding.chipThienEditorIced.setChecked(temps.contains(Temperature.ICED));
        binding.chipThienEditorSugar0.setChecked(sugars.contains(SugarLevel.P0));
        binding.chipThienEditorSugar30.setChecked(sugars.contains(SugarLevel.P30));
        binding.chipThienEditorSugar50.setChecked(sugars.contains(SugarLevel.P50));
        binding.chipThienEditorSugar70.setChecked(sugars.contains(SugarLevel.P70));
        binding.chipThienEditorSugar100.setChecked(sugars.contains(SugarLevel.P100));
    }
    private static <T extends Enum<T>> List<T> enumValues(List<String> names, Class<T> type) {
        List<T> result = new ArrayList<>();
        if (names != null) for (String name : names) {
            try { result.add(Enum.valueOf(type, name)); }
            catch (IllegalArgumentException ignored) { }
        }
        return result;
    }
    private void setupToppings(@Nullable Bundle state) {
        toppings.clear();
        binding.llThienEditorToppings.removeAllViews();
        List<String> selected = original == null ? new ArrayList<>() : original.getToppingIds();
        boolean all = selected.isEmpty();
        if (state != null && state.containsKey(STATE_TOPPINGS)) selected = state.getStringArrayList(STATE_TOPPINGS);
        if (state != null && state.containsKey(STATE_ALL)) all = state.getBoolean(STATE_ALL);
        if (selected == null) selected = new ArrayList<>();
        for (Topping item : catalog.getAllToppings()) {
            MaterialCheckBox checkbox = new MaterialCheckBox(requireContext());
            checkbox.setText(getString(R.string.thien_editor_topping_option, item.getName(),
                    NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(item.getPrice()),
                    item.isAvailable() ? "" : getString(R.string.thien_editor_topping_unavailable)));
            checkbox.setChecked(selected.contains(item.getId()));
            // Out-of-stock toppings may retain historical selection but cannot be newly checked.
            checkbox.setEnabled(item.isAvailable() || checkbox.isChecked());
            checkbox.setOnCheckedChangeListener((button, checked) -> { if (checked) binding.cbThienEditorAllToppings.setChecked(false); });
            binding.llThienEditorToppings.addView(checkbox);
            toppings.add(new ToppingChoice(item.getId(), item.isAvailable(), checkbox));
        }
        binding.cbThienEditorAllToppings.setOnCheckedChangeListener((button, checked) -> {
            for (ToppingChoice item : toppings)
                item.checkbox.setEnabled(!checked && (item.available || item.checkbox.isChecked()));
        });
        binding.cbThienEditorAllToppings.setChecked(all);
        for (ToppingChoice item : toppings)
            item.checkbox.setEnabled(!all && (item.available || item.checkbox.isChecked()));
    }
    private static final class ToppingChoice {
        final String id;
        final boolean available;
        final MaterialCheckBox checkbox;
        ToppingChoice(String id, boolean available, MaterialCheckBox checkbox) {
            this.id = id; this.available = available; this.checkbox = checkbox;
        }
    }
    private void addTopping() {
        LinearLayout fields = new LinearLayout(requireContext());
        fields.setOrientation(LinearLayout.VERTICAL);
        int inset = (int) (20 * getResources().getDisplayMetrics().density);
        fields.setPadding(inset, 8, inset, 0);
        EditText name = new EditText(requireContext());
        name.setSingleLine(true);
        name.setHint(R.string.thien_editor_topping_name_hint);
        fields.addView(name);
        EditText price = new EditText(requireContext());
        price.setSingleLine(true);
        price.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        price.setHint(R.string.thien_editor_topping_price_hint);
        fields.addView(price);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.thien_editor_add_topping)
                .setView(fields)
                .setNegativeButton(R.string.thien_editor_cancel, null)
                .setPositiveButton(R.string.thien_editor_create_topping, null)
                .create();
        dialog.setOnShowListener(shown -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = name.getText().toString().trim();
            long amount = ProductEditorFormPolicy.parseAmount(price.getText().toString());
            if (title.isEmpty() || amount < 0L) {
                name.setError(getString(R.string.thien_editor_topping_invalid));
                return;
            }
            // Topping is a separate shared model; saving it is immediate.
            try {
                Bundle saved = new Bundle();
                saved.putStringArrayList(STATE_TOPPINGS, new ArrayList<>(chosenToppingIds()));
                saved.putBoolean(STATE_ALL, binding.cbThienEditorAllToppings.isChecked());
                catalog.saveTopping(new Topping("seller_top_" + UUID.randomUUID(), title, amount));
                setupToppings(saved);
                dialog.dismiss();
            } catch (RuntimeException ignored) {
                name.setError(getString(R.string.thien_editor_save_failed));
            }
        }));
        dialog.show();
    }
    private void chooseImageSource() {
        String[] options = {
                getString(R.string.thien_editor_pick_device),
                getString(R.string.thien_editor_pick_sample)
        };
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.thien_editor_choose_image)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        imagePickerLauncher.launch("image/*");
                    } else {
                        pickPresetImage();
                    }
                }).show();
    }
    private void pickPresetImage() {
        String[] choices = getResources().getStringArray(R.array.thien_editor_sample_images);
        new AlertDialog.Builder(requireContext()).setTitle(R.string.thien_editor_choose_image)
                .setItems(choices, (dialog, which) -> { imageKey = IMAGE_KEYS[which]; updateImage(); }).show();
    }
    private void updateImage() {
        if (imageKey != null && (imageKey.startsWith("content://") || imageKey.startsWith("file://") || imageKey.startsWith("http"))) {
            Glide.with(this).load(Uri.parse(imageKey)).centerCrop().into(binding.ivThienEditorPhoto);
        } else {
            Glide.with(this).load(LocalImages.resolve(imageKey)).centerCrop().into(binding.ivThienEditorPhoto);
        }
    }
    private List<Temperature> chosenTemperatures() {
        List<Temperature> result = new ArrayList<>();
        if (binding.chipThienEditorHot.isChecked()) result.add(Temperature.HOT);
        if (binding.chipThienEditorIced.isChecked()) result.add(Temperature.ICED);
        return result;
    }
    private List<SugarLevel> chosenSugarLevels() {
        List<SugarLevel> result = new ArrayList<>();
        if (binding.chipThienEditorSugar0.isChecked()) result.add(SugarLevel.P0);
        if (binding.chipThienEditorSugar30.isChecked()) result.add(SugarLevel.P30);
        if (binding.chipThienEditorSugar50.isChecked()) result.add(SugarLevel.P50);
        if (binding.chipThienEditorSugar70.isChecked()) result.add(SugarLevel.P70);
        if (binding.chipThienEditorSugar100.isChecked()) result.add(SugarLevel.P100);
        return result;
    }
    private List<String> chosenToppingIds() {
        List<String> result = new ArrayList<>();
        if (binding.cbThienEditorAllToppings.isChecked()) return result;
        for (ToppingChoice topping : toppings) if (topping.checkbox.isChecked()) result.add(topping.id);
        return result;
    }
    private void save() {
        int chosenCategory = binding.spThienEditorCategory.getSelectedItemPosition();
        String category = categories.isEmpty() || chosenCategory < 0 || chosenCategory >= categories.size()
                ? null : categories.get(chosenCategory).getId();
        List<Temperature> temps = chosenTemperatures();
        List<SugarLevel> sugars = chosenSugarLevels();
        boolean all = binding.cbThienEditorAllToppings.isChecked();
        List<String> chosen = chosenToppingIds();
        ProductEditorFormPolicy.Result valid = ProductEditorFormPolicy.validate(
                binding.etThienEditorName.getText().toString(), category,
                binding.etThienEditorPriceS.getText().toString(),
                binding.etThienEditorPriceM.getText().toString(),
                binding.etThienEditorPriceL.getText().toString(), temps, sugars, all, chosen);
        if (!valid.isValid()) { showError(errorText(valid.issue)); return; }
        String id = original == null ? "seller_" + UUID.randomUUID() : original.getId();
        if (original != null && catalog.getProductForSeller(id) == null) {
            showError(R.string.thien_editor_missing); return;
        }
        Map<Size, Long> prices = new EnumMap<>(Size.class);
        prices.put(Size.S, valid.priceS); prices.put(Size.M, valid.priceM); prices.put(Size.L, valid.priceL);
        Product updated = new Product(id, valid.name, valid.priceS, imageKey,
                binding.etThienEditorDescription.getText().toString().trim(), category,
                prices, temps, sugars, chosen,
                binding.swThienEditorInStock.isChecked(), binding.swThienEditorVisible.isChecked());
        try {
            binding.btnThienEditorSave.setEnabled(false);
            catalog.saveProduct(updated);
            notifyChanged();
            dismiss();
        } catch (RuntimeException error) {
            binding.btnThienEditorSave.setEnabled(true);
            showError(R.string.thien_editor_save_failed);
        }
    }
    private int errorText(ProductEditorFormPolicy.Issue issue) {
        switch (issue) {
            case NAME: return R.string.thien_editor_error_name;
            case CATEGORY: return R.string.thien_editor_error_category;
            case PRICE: return R.string.thien_editor_error_price;
            case TEMPERATURE: return R.string.thien_editor_error_temperature;
            case SUGAR: return R.string.thien_editor_error_sugar;
            case TOPPING: return R.string.thien_editor_error_topping;
            default: return R.string.thien_editor_save_failed;
        }
    }
    private void showError(int error) {
        binding.tvThienEditorError.setText(error);
        binding.tvThienEditorError.setVisibility(View.VISIBLE);
        binding.svThienEditorFields.smoothScrollTo(0, 0);
    }
    private void confirmDelete() {
        if (original == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.thien_seller_product_delete_title)
                .setMessage(getString(R.string.thien_seller_product_delete_body, original.getName()))
                .setNegativeButton(R.string.thien_seller_product_cancel, null)
                .setPositiveButton(R.string.thien_seller_product_delete_confirm, (d, which) -> {
                    try {
                        if (!catalog.deleteProduct(original.getId())) {
                            showError(R.string.thien_editor_missing); return;
                        }
                        notifyChanged(); dismiss();
                    } catch (RuntimeException ignored) { showError(R.string.thien_editor_save_failed); }
                }).show();
    }
    private void notifyChanged() { getParentFragmentManager().setFragmentResult(RESULT_KEY, new Bundle()); }
    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        if (binding != null) {
            state.putString(STATE_IMAGE, imageKey);
            int chosenCategory = binding.spThienEditorCategory.getSelectedItemPosition();
            if (categories != null && chosenCategory >= 0 && chosenCategory < categories.size())
                state.putString(STATE_CATEGORY, categories.get(chosenCategory).getId());
            ArrayList<String> temps = new ArrayList<>();
            for (Temperature t : chosenTemperatures()) temps.add(t.name());
            state.putStringArrayList(STATE_TEMPS, temps);
            ArrayList<String> sugars = new ArrayList<>();
            for (SugarLevel sugar : chosenSugarLevels()) sugars.add(sugar.name());
            state.putStringArrayList(STATE_SUGARS, sugars);
            state.putStringArrayList(STATE_TOPPINGS, new ArrayList<>(chosenToppingIds()));
            state.putBoolean(STATE_ALL, binding.cbThienEditorAllToppings.isChecked());
        }
        super.onSaveInstanceState(state);
    }
    @Override public void onDestroyView() {
        binding = null;
        toppings.clear();
        super.onDestroyView();
    }
}
