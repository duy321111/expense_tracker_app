package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker_app.R;
import com.google.android.material.appbar.MaterialToolbar;

public class TransactionDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.transaction_detail);

        // Toolbar back
        MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> onBackPressed()); // hoặc finish()


        // ---- dữ liệu demo nhận từ Intent (có thể null) ----
        String amount   = getIntent().getStringExtra("amount");
        String category = getIntent().getStringExtra("category");
        String note     = getIntent().getStringExtra("note");
        String date     = getIntent().getStringExtra("date");
        String method   = getIntent().getStringExtra("method");
        String address  = getIntent().getStringExtra("address");

        // Ô số tiền
        TextView tvAmount = findViewById(R.id.tvAmount);
        if (amount != null) tvAmount.setText(amount);

        // ===== set nội dung cho các include =====
        bindRow(findViewById(R.id.rowCategory),
                getCategoryIcon(category), "Danh mục", safe(category, "Ăn uống"));

        bindRow(findViewById(R.id.rowNote),
                R.drawable.ic_note, "Ghi chú", safe(note, "Đi ăn mì cay"));

        bindRow(findViewById(R.id.rowDate),
                R.drawable.ic_calendar, "Ngày", safe(date, "16/03/2022"));

        bindRow(findViewById(R.id.rowMethod),
                R.drawable.ic_wallet, "Hình thức", safe(method, "Tiền mặt"));

        bindRow(findViewById(R.id.rowLocation),
                R.drawable.ic_location, "Địa điểm", safe(address, "123 Nguyễn Khánh Toàn"));

        bindRow(findViewById(R.id.rowPhoto),
                R.drawable.ic_image, "Thêm hình ảnh", "Chưa có");

        // Hàng switch “Không tính vào báo cáo”
        View rowExclude = findViewById(R.id.rowExclude);
        ((ImageView) rowExclude.findViewById(R.id.leftIcon)).setImageResource(R.drawable.ic_report);
        ((TextView)  rowExclude.findViewById(R.id.tvTitle)).setText("Không tính vào báo cáo");

        Switch sw = rowExclude.findViewById(R.id.swExclude);
        TextView tvHint = rowExclude.findViewById(R.id.tvExcludeHint);
        sw.setOnCheckedChangeListener((buttonView, checked) ->
                tvHint.setText(checked
                        ? "Giao dịch sẽ không tính vào báo cáo."
                        : "Giao dịch sẽ được tính vào báo cáo."));
    }

    // ---- helpers ----
    private void bindRow(View row, int iconRes, String title, String value) {
        ((ImageView) row.findViewById(R.id.leftIcon)).setImageResource(iconRes);
        ((TextView)  row.findViewById(R.id.tvTitle)).setText(title);
        ((TextView)  row.findViewById(R.id.tvValue)).setText(value);
    }

    private String safe(String v, String def) { return v == null || v.isEmpty() ? def : v; }

    private int getCategoryIcon(String category) {
        if (category == null) return R.drawable.ic_category;
        switch (category) {
            case "Ăn uống":     return R.drawable.ic_food;
            case "Cà phê":      return R.drawable.ic_coffee;
            case "Mua sắm":     return R.drawable.ic_shopping;
            case "Giải trí":    return R.drawable.ic_entertainment;
            case "Đi lại":      return R.drawable.ic_transport;
            case "Y tế":        return R.drawable.ic_health;
            default:            return R.drawable.ic_category;
        }
    }
}
