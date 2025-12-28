package com.example.expense_tracker_app.ui.Budget;

import android.os.Bundle;
import android.app.AlertDialog;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker_app.R;
import com.google.android.material.button.MaterialButton;
import android.view.View;
import com.example.expense_tracker_app.data.model.Budget;
import com.example.expense_tracker_app.data.model.Category;
import com.example.expense_tracker_app.data.model.CategoryWithSubcategories;
import com.example.expense_tracker_app.data.model.TxType;
import com.example.expense_tracker_app.data.repository.BudgetRepository;
import com.example.expense_tracker_app.data.repository.TransactionRepository;
import com.example.expense_tracker_app.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

public class AddBudget extends AppCompatActivity {

    private EditText etBudgetName, etBudgetAmount;
    private LinearLayout llRepeat, llSelectCategory;
    private TextView tvRepeat, tvCategoryName;
    private ImageView ivCategoryIcon;
    private MaterialButton btnSaveBudget;

    private String repeatOption = "Không lặp lại";
    private String selectedCategory = "Chọn danh mục";
    private int selectedCategoryIcon = R.drawable.ic_food; // default icon

    private List<String> selectedCategories = new ArrayList<>();
    private List<Integer> selectedSubcategoryIds = new ArrayList<>();
    private Map<String, Integer> categoryIconMap = new HashMap<>(); // Tên -> icon

    private BudgetRepository budgetRepository;
    private TransactionRepository transactionRepository;
    private UserRepository userRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget_add);

        // Toolbar back giống trang cá nhân
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarCreateBudget);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tạo ngân sách mới");
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

        // Fallback icon map (nếu không tìm được icon theo tên trong DB)
        categoryIconMap.put("Ăn uống", R.drawable.ic_cat_food);
        categoryIconMap.put("Cà phê", R.drawable.ic_cat_coffee);
        categoryIconMap.put("Đi chợ / Siêu thị", R.drawable.ic_cat_groceries);
        categoryIconMap.put("Điện", R.drawable.ic_cat_electric);
        categoryIconMap.put("Nước", R.drawable.ic_cat_water);
        categoryIconMap.put("Internet", R.drawable.ic_cat_internet);
        categoryIconMap.put("Di chuyển", R.drawable.ic_cat_transport);
        categoryIconMap.put("GAS", R.drawable.ic_cat_gas);

        // Chọn lặp lại
        llRepeat.setOnClickListener(v -> {
            String[] options = {"Không lặp lại", "Hàng tháng", "Hàng năm"};
            new AlertDialog.Builder(AddBudget.this)
                    .setTitle("Chọn lặp lại")
                    .setItems(options, (dialog, which) -> {
                        repeatOption = options[which];
                        tvRepeat.setText(repeatOption);
                    }).show();
        });

        // Chọn danh mục (UI giống AddTransaction, nhưng cho phép chọn nhiều)
        llSelectCategory.setOnClickListener(v -> {
            int userId = getUserId();
            List<CategoryWithSubcategories> data = transactionRepository.categoriesWithSubcategories(TxType.EXPENSE, userId);

            com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(AddBudget.this);

            ScrollView scrollView = new ScrollView(AddBudget.this);
            LinearLayout container = new LinearLayout(AddBudget.this);
            container.setOrientation(LinearLayout.VERTICAL);
            int pad = (int) (16 * getResources().getDisplayMetrics().density);
            container.setPadding(pad, pad, pad, pad);
            scrollView.addView(container);

            for (CategoryWithSubcategories cws : data) {
                if (cws == null || cws.category == null) continue;

                TextView title = new TextView(AddBudget.this);
                title.setText(cws.category.name);
                title.setTextSize(16);
                title.setTextColor(getResources().getColor(R.color.neutral_900, null));
                LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                titleLp.bottomMargin = (int) (8 * getResources().getDisplayMetrics().density);
                title.setLayoutParams(titleLp);
                container.addView(title);

                GridLayout grid = new GridLayout(AddBudget.this);
                grid.setColumnCount(4);
                grid.setUseDefaultMargins(true);

                if (cws.subcategories != null) {
                    for (com.example.expense_tracker_app.data.model.Subcategory sub : cws.subcategories) {
                        LinearLayout item = new LinearLayout(AddBudget.this);
                        item.setOrientation(LinearLayout.VERTICAL);
                        item.setGravity(android.view.Gravity.CENTER);
                        int itemPad = (int) (8 * getResources().getDisplayMetrics().density);
                        item.setPadding(itemPad, itemPad, itemPad, itemPad);
                        String label = cws.category.name + " > " + sub.name;
                        boolean isSelected = selectedCategories.contains(label);
                        item.setBackgroundResource(isSelected ? R.drawable.bg_chip_category_selected : R.drawable.bg_chip_category_state);

                        FrameLayout iconBg = new FrameLayout(AddBudget.this);
                        int bgSize = (int) (48 * getResources().getDisplayMetrics().density);
                        FrameLayout.LayoutParams bgParams = new FrameLayout.LayoutParams(bgSize, bgSize);
                        iconBg.setLayoutParams(bgParams);
                        iconBg.setBackgroundResource(R.drawable.bg_icon_round_accent_1);

                        ImageView iv = new ImageView(AddBudget.this);
                        int resId = getResources().getIdentifier(sub.icon, "drawable", getPackageName());
                        iv.setImageResource(resId != 0 ? resId : R.drawable.ic_category);
                        iv.setColorFilter(android.graphics.Color.WHITE);
                        int iconSize = (int) (24 * getResources().getDisplayMetrics().density);
                        FrameLayout.LayoutParams iconLp = new FrameLayout.LayoutParams(iconSize, iconSize);
                        iconLp.gravity = android.view.Gravity.CENTER;
                        iv.setLayoutParams(iconLp);
                        iconBg.addView(iv);

                        TextView tv = new TextView(AddBudget.this);
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

                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.width = 0;
                        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
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

            Button btnDone = new Button(AddBudget.this);
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

            long createdAt = System.currentTimeMillis();
            Calendar now = Calendar.getInstance();
            int currMonth = now.get(Calendar.MONTH) + 1; // Calendar.MONTH: 0-based, nên +1
            int currYear = now.get(Calendar.YEAR);
            Budget budget = new Budget(
                name,
                new ArrayList<>(selectedCategories),
                amount,
                0d,
                repeatOption,
                createdAt,
                currMonth,
                currYear,
                new ArrayList<>(selectedSubcategoryIds),
                getUserId()
            );
            boolean inserted = budgetRepository.addBudget(budget);
            if (inserted) {
                Toast.makeText(AddBudget.this, "Đã lưu ngân sách thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddBudget.this, "Lưu ngân sách thất bại!", Toast.LENGTH_SHORT).show();
            }
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

    private int getUserId() {
        try {
            if (userRepository == null) userRepository = new UserRepository(this);
            com.example.expense_tracker_app.data.model.User u = userRepository.getLoggedInUser();
            return u != null ? u.id : 1;
        } catch (Exception e) {
            return 1;
        }
    }
}
