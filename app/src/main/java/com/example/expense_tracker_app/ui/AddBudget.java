package com.example.expense_tracker_app.ui;

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

public class AddBudget extends AppCompatActivity {

    private EditText etBudgetName, etBudgetAmount;
    private LinearLayout llRepeat, llSelectCategory;
    private TextView tvRepeat, tvCategoryName;
    private ImageView ivCategoryIcon;
    private MaterialButton btnSaveBudget;

    private String repeatOption = "Không lặp lại";
    private String selectedCategory = "Chọn danh mục";
    private int selectedCategoryIcon = R.drawable.ic_food; // default icon

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget_add);

        // Ánh xạ view
        etBudgetName = findViewById(R.id.etBudgetName);
        etBudgetAmount = findViewById(R.id.etBudgetAmount);
        llRepeat = findViewById(R.id.llRepeat);
        tvRepeat = findViewById(R.id.tvRepeat);
        llSelectCategory = findViewById(R.id.llSelectCategory);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        ivCategoryIcon = findViewById(R.id.ivCategoryIcon);
        btnSaveBudget = findViewById(R.id.btnSaveBudget);

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

            Toast.makeText(AddBudget.this,
                    "Ngân sách đã lưu:\nTên: " + name +
                            "\nSố tiền: " + amount +
                            "\nDanh mục: " + selectedCategory +
                            "\nLặp lại: " + repeatOption,
                    Toast.LENGTH_LONG).show();

            finish();
        });
    }
}
