package com.example.expense_tracker_app.ui.Budget;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Budget;
import com.example.expense_tracker_app.data.repository.BudgetRepository;
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
    private Map<String, Integer> categoryIconMap = new HashMap<>();

    private BudgetRepository budgetRepository;
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

        // Chuẩn bị hashmap tên-danh-mục <=> icon
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
            com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog =
                    new com.google.android.material.bottomsheet.BottomSheetDialog(EditBudget.this);
            View sheetView = getLayoutInflater().inflate(R.layout.sheet_pick_category_add_budget, null);
            bottomSheetDialog.setContentView(sheetView);

            LinearLayout catFood = sheetView.findViewById(R.id.cat_food);
            LinearLayout catCoffee = sheetView.findViewById(R.id.cat_coffee);
            LinearLayout catGroceries = sheetView.findViewById(R.id.cat_groceries_market);

            Map<LinearLayout, String> linearToCat = new HashMap<>();
            linearToCat.put(catFood, "Ăn uống");
            linearToCat.put(catCoffee, "Cà phê");
            linearToCat.put(catGroceries, "Đi chợ / Siêu thị");

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

            bottomSheetDialog.setOnDismissListener(dialogInterface -> {
                tvCategoryName.setText(selectedCategories.isEmpty() ? "Chọn danh mục" : String.join(", ", selectedCategories));
                if(!selectedCategories.isEmpty()) {
                    Integer icon = categoryIconMap.get(selectedCategories.get(0));
                    ivCategoryIcon.setImageResource(icon != null ? icon : R.drawable.ic_cat_food);
                } else {
                    ivCategoryIcon.setImageResource(R.drawable.ic_cat_food);
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

            if (currentBudget == null || budgetId <= 0) {
                Toast.makeText(this, "Không tìm thấy ngân sách để cập nhật", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật thông tin vào budget hiện tại
            currentBudget.setName(name);
            currentBudget.setAmount(amount);
            currentBudget.setCategories(new ArrayList<>(selectedCategories));
            currentBudget.setPeriod(repeatOption);

            boolean updated = budgetRepository.updateBudget(currentBudget);
            if (updated) {
                Toast.makeText(EditBudget.this, "Đã cập nhật ngân sách thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditBudget.this, "Cập nhật ngân sách thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

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
    }
}
