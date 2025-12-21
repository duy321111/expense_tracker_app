package com.example.expense_tracker_app.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.repository.TransactionRepository;
import com.example.expense_tracker_app.ui.adapter.TransactionAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.time.LocalDate;

public class TransactionHistoryFragment extends AppCompatActivity {

    private RecyclerView rvTransactions;
    private ImageView btnBack;
    private LinearLayout layoutDatePicker; // Layout nút bấm chọn tháng
    private TextView tvCurrentMonthDisplay; // Text hiển thị "Tháng 12, 2025"

    private TransactionAdapter adapter;
    private TransactionRepository repository;

    // Biến lưu tháng đang chọn để hiển thị dữ liệu
    private LocalDate selectedDate = LocalDate.now();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_transaction_history);

        // 1. Ánh xạ View
        rvTransactions = findViewById(R.id.rv_transactions);
        btnBack = findViewById(R.id.btn_back);
        layoutDatePicker = findViewById(R.id.layout_date_picker);
        tvCurrentMonthDisplay = findViewById(R.id.tv_current_month_display);

        // 2. Setup RecyclerView
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this);
        rvTransactions.setAdapter(adapter);

        // 3. Khởi tạo Repository
        repository = new TransactionRepository(getApplication());

        // 4. Xử lý sự kiện
        btnBack.setOnClickListener(v -> finish());

        // Sự kiện bấm vào thanh chọn tháng
        layoutDatePicker.setOnClickListener(v -> showMonthPicker());

        // 5. Tải dữ liệu ban đầu
        updateMonthDisplay();
        loadDataForSelectedMonth();
    }

    // Cập nhật text trên màn hình chính
    private void updateMonthDisplay() {
        String formattedDate = String.format("%02d-%d", selectedDate.getMonthValue(), selectedDate.getYear());
        tvCurrentMonthDisplay.setText(formattedDate);
    }

    private void loadDataForSelectedMonth() {
        repository.getTransactionsByMonth(1, selectedDate).observe(this, transactions -> {
            if (transactions != null) {
                adapter.setData(transactions);
            }
        });
    }

    // --- LOGIC HIỂN THỊ POPUP CHỌN THÁNG ---
    private void showMonthPicker() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_month_picker, null);
        dialog.setContentView(view);

        // Ánh xạ View trong Dialog
        ImageView btnPrevYear = view.findViewById(R.id.btn_prev_year);
        ImageView btnNextYear = view.findViewById(R.id.btn_next_year);
        TextView tvDialogYear = view.findViewById(R.id.tv_dialog_year);
        GridLayout gridMonths = view.findViewById(R.id.grid_months);

        // Biến tạm để lưu năm đang chọn trong dialog (không ảnh hưởng selectedDate chính cho đến khi chọn tháng)
        final int[] dialogYear = {selectedDate.getYear()};
        tvDialogYear.setText(String.valueOf(dialogYear[0]));

        // Xử lý chuyển năm
        btnPrevYear.setOnClickListener(v -> {
            dialogYear[0]--;
            tvDialogYear.setText(String.valueOf(dialogYear[0]));
            renderMonthGrid(dialog, gridMonths, dialogYear[0]); // Vẽ lại lưới tháng
        });

        btnNextYear.setOnClickListener(v -> {
            dialogYear[0]++;
            tvDialogYear.setText(String.valueOf(dialogYear[0]));
            renderMonthGrid(dialog, gridMonths, dialogYear[0]);
        });

        // Vẽ lưới 12 tháng lần đầu
        renderMonthGrid(dialog, gridMonths, dialogYear[0]);

        dialog.show();
    }

    private void renderMonthGrid(BottomSheetDialog dialog, GridLayout grid, int year) {
        grid.removeAllViews(); // Xóa view cũ

        for (int i = 1; i <= 12; i++) {
            TextView tv = new TextView(this);
            tv.setText("Th. " + i);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(16, 24, 16, 24);

            // Cấu hình LayoutParams cho Grid
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Chia đều 4 cột
            params.setMargins(8, 8, 8, 8);
            tv.setLayoutParams(params);

            // Kiểm tra xem tháng này có phải tháng đang được chọn của App không
            boolean isSelected = (year == selectedDate.getYear() && i == selectedDate.getMonthValue());

            if (isSelected) {
                // Style cho tháng đang chọn: Nền màu Tím/Xanh (Primary), chữ Trắng
                tv.setBackgroundResource(R.drawable.bg_icon_round_primary_1); // Dùng drawable bo tròn màu primary
                tv.setTextColor(Color.WHITE);
                tv.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                // Style cho tháng thường: Nền xám nhạt, chữ đen
                tv.setBackgroundResource(R.drawable.bg_card_neutral_50); // Dùng drawable bo tròn màu xám
                tv.setTextColor(getColor(R.color.neutral_900));
            }

            int finalMonth = i;
            tv.setOnClickListener(v -> {
                // Khi người dùng chọn tháng:
                selectedDate = LocalDate.of(year, finalMonth, 1);

                // 1. Cập nhật Text màn hình chính
                updateMonthDisplay();
                // 2. Tải lại dữ liệu
                loadDataForSelectedMonth();
                // 3. Đóng dialog
                dialog.dismiss();
            });

            grid.addView(tv);
        }
    }
}