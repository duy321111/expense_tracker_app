package com.example.expense_tracker_app.ui.Budget;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker_app.R;
import com.google.android.material.button.MaterialButton;
import android.view.View;
import com.example.expense_tracker_app.data.model.Budget;
import com.example.expense_tracker_app.data.repository.BudgetRepository;

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
    private Map<String, Integer> categoryIconMap = new HashMap<>(); // Tên -> icon

    private BudgetRepository budgetRepository;

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

        // Chuẩn bị hashmap tên-danh-mục <=> icon
        categoryIconMap.put("Ăn uống", R.drawable.ic_cat_food);
        categoryIconMap.put("Cà phê", R.drawable.ic_cat_coffee);
            categoryIconMap.put("Đi chợ / Siêu thị", R.drawable.ic_cat_groceries);
            categoryIconMap.put("Điện", R.drawable.ic_cat_electric);
            categoryIconMap.put("Nước", R.drawable.ic_cat_water);
            categoryIconMap.put("Internet", R.drawable.ic_cat_internet);
            categoryIconMap.put("Di chuyển", R.drawable.ic_cat_transport);
            categoryIconMap.put("GAS", R.drawable.ic_cat_gas);
        // Có thể bổ sung các cặp tên -> icon khác

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

        // Chọn danh mục
        llSelectCategory.setOnClickListener(v -> {
            com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(AddBudget.this);
            View sheetView = getLayoutInflater().inflate(R.layout.sheet_pick_category_add_budget, null);
            bottomSheetDialog.setContentView(sheetView);

            LinearLayout catFood = sheetView.findViewById(R.id.cat_food);
            LinearLayout catCoffee = sheetView.findViewById(R.id.cat_coffee);
            LinearLayout catGroceries = sheetView.findViewById(R.id.cat_groceries_market);
            // ... thêm danh mục khác tương tự nếu muốn

            // Multi select bằng việc toggle background khi click
            Map<LinearLayout, String> linearToCat = new HashMap<>();
            linearToCat.put(catFood, "Ăn uống");
            linearToCat.put(catCoffee, "Cà phê");
            linearToCat.put(catGroceries, "Đi chợ / Siêu thị");
            // ...

            // Đánh dấu ô nào đã chọn
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

            // Khi người dùng nhấn xong sheet, cập nhật lại giao diện
            bottomSheetDialog.setOnDismissListener(dialogInterface -> {
                // Gộp tên danh mục ra TextView
                tvCategoryName.setText(selectedCategories.isEmpty() ? "Chọn danh mục" : String.join(", ", selectedCategories));
                // Nếu chỉ 1 icon -> hiện icon, nhiều -> dùng icon đầu tiên (nâng cao: group avatar)
                if(!selectedCategories.isEmpty()) {
                    Integer icon = categoryIconMap.get(selectedCategories.get(0));
                    ivCategoryIcon.setImageResource(icon != null ? icon : R.drawable.ic_cat_food);
                } else {
                    ivCategoryIcon.setImageResource(R.drawable.ic_cat_food); // default
                }
            });

            bottomSheetDialog.show();
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
                currYear
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
}
