package com.example.expense_tracker_app.ui.Budget;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Budget;
import com.example.expense_tracker_app.data.model.Category;
import com.example.expense_tracker_app.data.model.User;
import com.example.expense_tracker_app.data.repository.BudgetRepository;
import com.example.expense_tracker_app.data.repository.TransactionRepository;
import com.example.expense_tracker_app.data.repository.UserRepository;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditBudget extends AppCompatActivity {

    private EditText etBudgetName, etBudgetAmount;
    private LinearLayout llRepeat, llSelectCategory;
    private TextView tvRepeat, tvCategoryName;
    private ImageView ivCategoryIcon;
    private MaterialButton btnSaveBudget;

    private String repeatOption = "Không lặp lại";
    private List<String> selectedCategories = new ArrayList<>();
    private List<Integer> selectedSubcategoryIds = new ArrayList<>();
    private Map<String, Integer> categoryIconMap = new HashMap<>();
    private List<Category> allCategories = new ArrayList<>();

    private BudgetRepository budgetRepository;
    private TransactionRepository transactionRepository;
    private UserRepository userRepository;
    private Budget currentBudget;
    private int budgetId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget_edit);

        // Toolbar back giống trang cá nhân
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarCreateBudget);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chỉnh sửa ngân sách");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Ánh xạ view
        etBudgetName = findViewById(R.id.etBudgetName);
        etBudgetAmount = findViewById(R.id.etBudgetAmount);
        llRepeat = findViewById(R.id.llRepeat);
        tvRepeat = findViewById(R.id.tvRepeat);
        llSelectCategory = findViewById(R.id.llSelectCategory);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        ivCategoryIcon = findViewById(R.id.ivCategoryIcon);
        btnSaveBudget = findViewById(R.id.btnSaveBudget);

        budgetRepository = new BudgetRepository(this);
        transactionRepository = new TransactionRepository(getApplication());
        userRepository = new UserRepository(this);

        // Load categories từ database theo userId
        int userId = getUserId();
        loadCategoriesFromDatabase(userId);

        // Chuẩn bị hashmap tên-danh-mục <=> icon (fallback)
        categoryIconMap.put("Ăn uống", R.drawable.ic_cat_food);
        categoryIconMap.put("Cà phê", R.drawable.ic_cat_coffee);
        categoryIconMap.put("Đi chợ / Siêu thị", R.drawable.ic_cat_groceries);
        categoryIconMap.put("Điện", R.drawable.ic_cat_electric);
        categoryIconMap.put("Nước", R.drawable.ic_cat_water);
        categoryIconMap.put("Internet", R.drawable.ic_cat_internet);
        categoryIconMap.put("Di chuyển", R.drawable.ic_cat_transport);
        categoryIconMap.put("GAS", R.drawable.ic_cat_gas);

        // Load dữ liệu ngân sách hiện tại từ Intent
        loadBudgetData();

        // Chọn lặp lại
        llRepeat.setOnClickListener(v -> {
            String[] options = {"Không lặp lại", "Hàng tháng", "Hàng năm"};
            new AlertDialog.Builder(EditBudget.this)
                    .setTitle("Chọn lặp lại")
                    .setItems(options, (dialog, which) -> {
                        repeatOption = options[which];
                        tvRepeat.setText(repeatOption);
                    }).show();
        });

        // Chọn danh mục
        llSelectCategory.setOnClickListener(v -> {

            List<com.example.expense_tracker_app.data.model.CategoryWithSubcategories> data = transactionRepository.categoriesWithSubcategories(com.example.expense_tracker_app.data.model.TxType.EXPENSE, userId);

            com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(EditBudget.this);

            android.widget.ScrollView scrollView = new android.widget.ScrollView(EditBudget.this);
            LinearLayout container = new LinearLayout(EditBudget.this);
            container.setOrientation(LinearLayout.VERTICAL);
            int pad = (int) (16 * getResources().getDisplayMetrics().density);
            container.setPadding(pad, pad, pad, pad);
            scrollView.addView(container);

            for (com.example.expense_tracker_app.data.model.CategoryWithSubcategories cws : data) {
                if (cws == null || cws.category == null) continue;

                TextView title = new TextView(EditBudget.this);
                title.setText(cws.category.name);
                title.setTextSize(16);
                title.setTextColor(getResources().getColor(R.color.neutral_900, null));
                LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                titleLp.bottomMargin = (int) (8 * getResources().getDisplayMetrics().density);
                title.setLayoutParams(titleLp);
                container.addView(title);

                android.widget.GridLayout grid = new android.widget.GridLayout(EditBudget.this);
                grid.setColumnCount(4);
                grid.setUseDefaultMargins(true);

                if (cws.subcategories != null) {
                    for (com.example.expense_tracker_app.data.model.Subcategory sub : cws.subcategories) {
                        LinearLayout item = new LinearLayout(EditBudget.this);
                        item.setOrientation(LinearLayout.VERTICAL);
                        item.setGravity(android.view.Gravity.CENTER);
                        int itemPad = (int) (8 * getResources().getDisplayMetrics().density);
                        item.setPadding(itemPad, itemPad, itemPad, itemPad);
                        String label = cws.category.name + " > " + sub.name;
                        boolean isSelected = selectedCategories.contains(label);
                        item.setBackgroundResource(isSelected ? R.drawable.bg_chip_category_selected : R.drawable.bg_chip_category_state);

                        android.widget.FrameLayout iconBg = new android.widget.FrameLayout(EditBudget.this);
                        int bgSize = (int) (48 * getResources().getDisplayMetrics().density);
                        android.widget.FrameLayout.LayoutParams bgParams = new android.widget.FrameLayout.LayoutParams(bgSize, bgSize);
                        iconBg.setLayoutParams(bgParams);
                        iconBg.setBackgroundResource(R.drawable.bg_icon_round_accent_1);

                        ImageView iv = new ImageView(EditBudget.this);
                        int resId = getResources().getIdentifier(sub.icon, "drawable", getPackageName());
                        iv.setImageResource(resId != 0 ? resId : R.drawable.ic_category);
                        iv.setColorFilter(android.graphics.Color.WHITE);
                        int iconSize = (int) (24 * getResources().getDisplayMetrics().density);
                        android.widget.FrameLayout.LayoutParams iconLp = new android.widget.FrameLayout.LayoutParams(iconSize, iconSize);
                        iconLp.gravity = android.view.Gravity.CENTER;
                        iv.setLayoutParams(iconLp);
                        iconBg.addView(iv);

                        TextView tv = new TextView(EditBudget.this);
                        tv.setText(sub.name);
                        tv.setGravity(android.view.Gravity.CENTER);
                        tv.setTextSize(12);
                        tv.setMaxLines(2);

                        item.addView(iconBg);
                        item.addView(tv);

                        item.setOnClickListener(v2 -> {
                            if (selectedCategories.contains(label)) {
                                selectedCategories.remove(label);
                                selectedSubcategoryIds.remove((Integer) sub.id);
                                item.setBackgroundResource(R.drawable.bg_chip_category_state);
                            } else {
                                selectedCategories.add(label);
                                selectedSubcategoryIds.add(sub.id);
                                item.setBackgroundResource(R.drawable.bg_chip_category_selected);
                            }
                        });

                        android.widget.GridLayout.LayoutParams params = new android.widget.GridLayout.LayoutParams();
                        params.width = 0;
                        params.columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f);
                        item.setLayoutParams(params);
                        grid.addView(item);
                    }
                }

                LinearLayout.LayoutParams gridLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                gridLp.bottomMargin = (int) (12 * getResources().getDisplayMetrics().density);
                grid.setLayoutParams(gridLp);
                container.addView(grid);

                // Map icon for the group
                if (cws.category.icon != null && !cws.category.icon.isEmpty()) {
                    int resId = getResources().getIdentifier(cws.category.icon, "drawable", getPackageName());
                    if (resId != 0) categoryIconMap.put(cws.category.name, resId);
                }
            }

            android.widget.Button btnDone = new android.widget.Button(EditBudget.this);
            btnDone.setText("Xong");
            LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            btnLp.topMargin = (int) (8 * getResources().getDisplayMetrics().density);
            btnDone.setLayoutParams(btnLp);
            btnDone.setOnClickListener(v3 -> {
                updateCategorySummaryUI();
                dialog.dismiss();
            });
            container.addView(btnDone);

            dialog.setContentView(scrollView);
            dialog.show();
        });

        // Lưu ngân sách
        btnSaveBudget.setOnClickListener(v -> {
            String name = etBudgetName.getText().toString().trim();
            String amountStr = etBudgetAmount.getText().toString().trim();

            if (name.isEmpty()) {
                etBudgetName.setError("Nhập tên ngân sách");
                return;
            }

            if (amountStr.isEmpty()) {
                etBudgetAmount.setError("Nhập số tiền");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                etBudgetAmount.setError("Số tiền không hợp lệ");
                return;
            }

            if (selectedCategories.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất một danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentBudget == null || budgetId <= 0) {
                Toast.makeText(this, "Không tìm thấy ngân sách để cập nhật", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật thông tin vào budget hiện tại
            currentBudget.setName(name);
            currentBudget.setAmount(amount);
            currentBudget.setCategories(new ArrayList<>(selectedCategories));
            currentBudget.setSubcategoryIds(new ArrayList<>(selectedSubcategoryIds));
            currentBudget.setPeriod(repeatOption);
            currentBudget.setUserId(getUserId());

            boolean updated = budgetRepository.updateBudget(currentBudget);
            if (updated) {
                Toast.makeText(EditBudget.this, "Đã cập nhật ngân sách thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditBudget.this, "Cập nhật ngân sách thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
    } // Kết thúc onCreate

    // ------------------------------------------------------------------------
    // ĐƯA CÁC HÀM NÀY VÀO BÊN TRONG CLASS (Xóa dấu } thừa nếu có ở trên)
    // ------------------------------------------------------------------------

    private void loadBudgetData() {
        if (getIntent() == null) return;

        budgetId = getIntent().getIntExtra("budget_id", -1);
        if (budgetId <= 0) {
            Toast.makeText(this, "Không tìm thấy ngân sách", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load budget từ database
        currentBudget = budgetRepository.getBudgetById(budgetId);
        if (currentBudget == null) {
            Toast.makeText(this, "Không tìm thấy ngân sách", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Populate form với dữ liệu hiện tại
        etBudgetName.setText(currentBudget.getName());
        etBudgetAmount.setText(String.valueOf((long)currentBudget.getAmount()));
        repeatOption = currentBudget.getPeriod();
        tvRepeat.setText(repeatOption);

        if (currentBudget.getCategories() != null && !currentBudget.getCategories().isEmpty()) {
            selectedCategories = new ArrayList<>(currentBudget.getCategories());
            tvCategoryName.setText(String.join(", ", selectedCategories));
            Integer icon = categoryIconMap.get(selectedCategories.get(0));
            ivCategoryIcon.setImageResource(icon != null ? icon : R.drawable.ic_cat_food);
        } else {
            selectedCategories = new ArrayList<>();
            tvCategoryName.setText("Chọn danh mục");
            ivCategoryIcon.setImageResource(R.drawable.ic_cat_food);
        }
        // Load subcategoryIds nếu có
        if (currentBudget.getSubcategoryIds() != null) {
            selectedSubcategoryIds = new ArrayList<>(currentBudget.getSubcategoryIds());
        } else {
            selectedSubcategoryIds = new ArrayList<>();
        }
    }

    private int getUserId() {
        try {
            User loggedUser = userRepository.getLoggedInUser();
            return loggedUser != null ? loggedUser.id : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    private void loadCategoriesFromDatabase(int userId) {
        new Thread(() -> {
            try {
                allCategories = transactionRepository.getCustomCategories(userId);
                // Chuẩn bị icon map từ categories
                for (Category cat : allCategories) {
                    if (cat.icon != null && !cat.icon.isEmpty()) {
                        try {
                            int iconResId = getResources().getIdentifier(cat.icon, "drawable", getPackageName());
                            if (iconResId != 0) {
                                categoryIconMap.put(cat.name, iconResId);
                            }
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showCategorySelectionOld(View sheetView, com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog) {
        // Fallback: nếu layout không có container_categories, sử dụng hardcode cũ
        LinearLayout catFood = sheetView.findViewById(R.id.cat_food);
        LinearLayout catCoffee = sheetView.findViewById(R.id.cat_coffee);
        LinearLayout catGroceries = sheetView.findViewById(R.id.cat_groceries_market);

        Map<LinearLayout, String> linearToCat = new HashMap<>();
        if (catFood != null) linearToCat.put(catFood, "Ăn uống");
        if (catCoffee != null) linearToCat.put(catCoffee, "Cà phê");
        if (catGroceries != null) linearToCat.put(catGroceries, "Đi chợ / Siêu thị");

        for (Map.Entry<LinearLayout, String> entry : linearToCat.entrySet()) {
            if (selectedCategories.contains(entry.getValue())) {
                entry.getKey().setBackgroundResource(R.drawable.bg_chip_category_selected);
            } else {
                entry.getKey().setBackgroundResource(R.drawable.bg_chip_category_state);
            }
        }

        for (Map.Entry<LinearLayout, String> entry : linearToCat.entrySet()) {
            entry.getKey().setOnClickListener(layoutView -> {
                String cat = entry.getValue();
                if (selectedCategories.contains(cat)) {
                    selectedCategories.remove(cat);
                    entry.getKey().setBackgroundResource(R.drawable.bg_chip_category_state);
                } else {
                    selectedCategories.add(cat);
                    entry.getKey().setBackgroundResource(R.drawable.bg_chip_category_selected);
                }
            });
        }

        bottomSheetDialog.setOnDismissListener(dialogInterface -> {
            updateCategorySummaryUI();
        });
    }

    private void updateCategorySummaryUI() {
        tvCategoryName.setText(selectedCategories.isEmpty() ? "Chọn danh mục" : String.join(", ", selectedCategories));
        if (!selectedCategories.isEmpty()) {
            String first = selectedCategories.get(0);
            String catForIcon = first.contains(" > ") ? first.substring(0, first.indexOf(" > ")) : first;
            Integer icon = categoryIconMap.get(catForIcon);
            ivCategoryIcon.setImageResource(icon != null ? icon : R.drawable.ic_cat_food);
        } else {
            ivCategoryIcon.setImageResource(R.drawable.ic_cat_food);
        }
    }

}