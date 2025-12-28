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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.database.AppDatabase;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.repository.TransactionRepository;
import com.example.expense_tracker_app.ui.adapter.TransactionAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Locale;

public class Transactions extends AppCompatActivity {

    private DonutChartView donutChart;
    private TextView tvTotalAmount, tvCenterTop;

    private RecyclerView rvTransactions;
    private TransactionAdapter txAdapter;
    private TransactionRepository repo;

    private int userId;

    private LinearLayout layoutDatePicker;
    private TextView tvCurrentMonthDisplay;
    private LocalDate selectedDate = LocalDate.now();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transactions);

        userId = getSharedPreferences("session", MODE_PRIVATE).getInt("user_id", -1);

        // init views
        donutChart = findViewById(R.id.donutChart);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvCenterTop = findViewById(R.id.tvCenterTop);

        // RecyclerView transactions
        rvTransactions = findViewById(R.id.rvTransactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        txAdapter = new TransactionAdapter(this);
        txAdapter.setListener(tx -> showDeleteConfirm(tx));
        rvTransactions.setAdapter(txAdapter);

        repo = new TransactionRepository(getApplication());

        // Date picker
        layoutDatePicker = findViewById(R.id.layout_date_picker);
        tvCurrentMonthDisplay = findViewById(R.id.tv_current_month_display);

        updateMonthDisplay();

        layoutDatePicker.setOnClickListener(v -> showMonthPicker());

        // load tháng hiện tại
        onMonthSelected(selectedDate.getYear(), selectedDate.getMonthValue());
    }

    private void onMonthSelected(int year, int month) {
        if (userId <= 0) {
            txAdapter.setData(Collections.emptyList());
            tvTotalAmount.setText("0 đ");
            tvCenterTop.setText("0 đ");
            if (donutChart != null) donutChart.setValues(new float[]{0f});
            return;
        }

        LocalDate selected = LocalDate.of(year, month, 1);

        repo.getTransactionsByMonth(userId, selected).observe(this, transactions -> {
            if (transactions == null) transactions = Collections.emptyList();

            txAdapter.setData(transactions);

            float total = 0f;
            for (int i = 0; i < transactions.size(); i++) {
                // t.amount phải tồn tại trong model Transaction của bạn
                total += Math.abs((float) transactions.get(i).amount);
            }

            tvTotalAmount.setText(formatVND(total));
            tvCenterTop.setText(total == 0 ? "0 đ" : formatVND(total));

            if (donutChart == null) return;

            if (total <= 0f || transactions.isEmpty()) {
                donutChart.setValues(new float[]{0f});
                return;
            }

            // donut tạm theo từng transaction
            float[] vals = new float[transactions.size()];
            for (int i = 0; i < transactions.size(); i++) {
                vals[i] = Math.abs((float) transactions.get(i).amount);
            }
            donutChart.setValues(vals);
        });
    }

    private void updateMonthDisplay() {
        String formatted = String.format(Locale.US, "%02d-%d",
                selectedDate.getMonthValue(), selectedDate.getYear());
        tvCurrentMonthDisplay.setText(formatted);
    }

    private String formatVND(float v) {
        java.text.NumberFormat f = java.text.NumberFormat.getInstance(new Locale("vi", "VN"));
        return f.format(Math.round(v)) + " đ";
    }

    // --- Month Picker BottomSheet ---
    private void showMonthPicker() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_month_picker, null);
        dialog.setContentView(view);

        ImageView btnPrevYear = view.findViewById(R.id.btn_prev_year);
        ImageView btnNextYear = view.findViewById(R.id.btn_next_year);
        TextView tvDialogYear = view.findViewById(R.id.tv_dialog_year);
        GridLayout gridMonths = view.findViewById(R.id.grid_months);

        final int[] dialogYear = { selectedDate.getYear() };
        tvDialogYear.setText(String.valueOf(dialogYear[0]));

        btnPrevYear.setOnClickListener(v -> {
            dialogYear[0]--;
            tvDialogYear.setText(String.valueOf(dialogYear[0]));
            renderMonthGrid(dialog, gridMonths, dialogYear[0]);
        });

        btnNextYear.setOnClickListener(v -> {
            dialogYear[0]++;
            tvDialogYear.setText(String.valueOf(dialogYear[0]));
            renderMonthGrid(dialog, gridMonths, dialogYear[0]);
        });

        renderMonthGrid(dialog, gridMonths, dialogYear[0]);
        dialog.show();
    }

    private void renderMonthGrid(BottomSheetDialog dialog, GridLayout grid, int year) {
        grid.removeAllViews();

        for (int i = 1; i <= 12; i++) {
            TextView tv = new TextView(this);
            tv.setText("Th. " + i);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(16, 24, 16, 24);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            tv.setLayoutParams(params);

            boolean isSelected = (year == selectedDate.getYear() && i == selectedDate.getMonthValue());

            if (isSelected) {
                tv.setBackgroundResource(R.drawable.bg_icon_round_primary_1);
                tv.setTextColor(Color.WHITE);
                tv.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tv.setBackgroundResource(R.drawable.bg_card_neutral_50);
                tv.setTextColor(getColor(R.color.neutral_900));
            }

            int finalMonth = i;
            tv.setOnClickListener(v -> {
                selectedDate = LocalDate.of(year, finalMonth, 1);
                updateMonthDisplay();

                // IMPORTANT: load đúng adapter/repo của màn này
                onMonthSelected(selectedDate.getYear(), selectedDate.getMonthValue());

                dialog.dismiss();
            });

            grid.addView(tv);
        }
    }

    private void showDeleteConfirm(Transaction tx) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa giao dịch?")
                .setMessage("Bạn có chắc muốn xóa giao dịch này không?")
                .setNegativeButton("Hủy", (d, w) -> d.dismiss())
                .setPositiveButton("Xóa", (d, w) -> {
                    // Xóa trong DB (nên làm ở background)
                    java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                        // CÁCH 1 (khuyến nghị): có delete(Transaction) trong TransactionDao
                        //AppDatabase.getInstance(getApplicationContext()).transactionDao().delete(tx);

                        // CÁCH 2: nếu bạn chỉ có deleteById
                        AppDatabase.getInstance(getApplicationContext()).transactionDao().deleteById(tx.id);

                        // Nếu bạn đang dùng Repository:
                        // repo.delete(tx);  (tùy bạn đã implement hay chưa)

                        runOnUiThread(() -> {
                            // Reload lại tháng đang chọn để cập nhật list + total + donut
                            onMonthSelected(selectedDate.getYear(), selectedDate.getMonthValue());
                            d.dismiss();
                        });
                    });
                })
                .show();
    }

}
