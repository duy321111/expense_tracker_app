package com.example.expense_tracker_app.ui.Loan;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.repository.TransactionRepository;
import com.example.expense_tracker_app.ui.adapter.TransactionAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Locale;

public class LoanTrackingActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;

    private LinearLayout layoutDatePicker;
    private TextView tvCurrentMonthDisplay;

    private TextView tvTotalAmount;

    private RecyclerView rvTransactions;
    private TransactionAdapter txAdapter;
    private TransactionRepository repo;

    private int userId;
    private LocalDate selectedDate = LocalDate.now(); // tháng đang xem

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_tracking);

        userId = getSharedPreferences("session", MODE_PRIVATE).getInt("user_id", -1);

        initViews();
        setupToolbarBack();

        repo = new TransactionRepository(getApplication());

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        txAdapter = new TransactionAdapter(this);
        rvTransactions.setAdapter(txAdapter);

        updateMonthDisplay();

        layoutDatePicker.setOnClickListener(v -> showMonthPicker());

        // load tháng hiện tại
        onMonthSelected(selectedDate.getYear(), selectedDate.getMonthValue());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);

        layoutDatePicker = findViewById(R.id.layout_date_picker);
        tvCurrentMonthDisplay = findViewById(R.id.tv_current_month_display);

        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        rvTransactions = findViewById(R.id.rvTransactions);
    }

    private void setupToolbarBack() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v ->
                    getOnBackPressedDispatcher().onBackPressed()
            );
        }
    }

    private void onMonthSelected(int year, int month) {
        selectedDate = LocalDate.of(year, month, 1);
        updateMonthDisplay();

        if (userId <= 0) {
            txAdapter.setData(Collections.emptyList());
            tvTotalAmount.setText("0 đ");
            return;
        }

        // ✅ chỉ BORROW + LEND (lọc trong repo/dao)
        repo.getLoanTransactionsByMonth(userId, selectedDate).observe(this, list -> {
            if (list == null) list = Collections.emptyList();

            txAdapter.setData(list);

            long total = 0L;
            for (Transaction t : list) {
                total += Math.abs(t.amount);
            }

            tvTotalAmount.setText(formatVND(total));
        });
    }

    private void updateMonthDisplay() {
        String formatted = String.format(Locale.US, "%02d-%d",
                selectedDate.getMonthValue(), selectedDate.getYear());
        tvCurrentMonthDisplay.setText(formatted);
    }

    private String formatVND(long v) {
        java.text.NumberFormat f = java.text.NumberFormat.getInstance(new Locale("vi", "VN"));
        return f.format(v) + " đ";
    }

    // ================= Month Picker BottomSheet (reuse từ Transactions) =================

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

        for (int m = 1; m <= 12; m++) {
            TextView tv = new TextView(this);
            tv.setText("Th. " + m);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(16, 24, 16, 24);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            tv.setLayoutParams(params);

            boolean isSelected = (year == selectedDate.getYear() && m == selectedDate.getMonthValue());

            if (isSelected) {
                tv.setBackgroundResource(R.drawable.bg_icon_round_primary_1);
                tv.setTextColor(Color.WHITE);
                tv.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tv.setBackgroundResource(R.drawable.bg_card_neutral_50);
                tv.setTextColor(getColor(R.color.neutral_900));
            }

            int finalMonth = m;
            tv.setOnClickListener(v -> {
                onMonthSelected(year, finalMonth);
                dialog.dismiss();
            });

            grid.addView(tv);
        }
    }
}
