package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker_app.R;

public class BudgetDetail extends AppCompatActivity {

    private TextView tvTotalAmount, tvCenterTop;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget_detail);

        // --- Toolbar ---
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // hiển thị nút back
        }

        // Xử lý back button của Toolbar
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed(); // quay lại activity trước đó
        });

        // --- init views ---
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvCenterTop = findViewById(R.id.tvCenterTop);

        // TextView "Sửa"
        TextView tvEdit = findViewById(R.id.tvEdit);
        tvEdit.setOnClickListener(v -> {
            Intent intent = new Intent(BudgetDetail.this, EditBudget.class);
            startActivity(intent);
        });
    }
}
