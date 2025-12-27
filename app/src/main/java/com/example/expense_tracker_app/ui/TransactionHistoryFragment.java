package com.example.expense_tracker_app.ui;

import android.app.AlertDialog; // Import AlertDialog
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.User;
import com.example.expense_tracker_app.data.repository.TransactionRepository;
import com.example.expense_tracker_app.data.repository.UserRepository;
import com.example.expense_tracker_app.ui.adapter.TransactionAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.time.LocalDate;

public class TransactionHistoryFragment extends AppCompatActivity {

    private RecyclerView rvTransactions;
    private ImageView btnBack;
    private LinearLayout layoutDatePicker;
    private TextView tvCurrentMonthDisplay;

    private TransactionAdapter adapter;
    private TransactionRepository repository;
    private UserRepository userRepository;

    private LocalDate selectedDate = LocalDate.now();
    private int currentUserId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_transaction_history);

        rvTransactions = findViewById(R.id.rv_transactions);
        btnBack = findViewById(R.id.btn_back);
        layoutDatePicker = findViewById(R.id.layout_date_picker);
        tvCurrentMonthDisplay = findViewById(R.id.tv_current_month_display);

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));

        // Cập nhật Adapter với sự kiện click xóa
        adapter = new TransactionAdapter(this, this::showDeleteDialog);
        rvTransactions.setAdapter(adapter);

        repository = new TransactionRepository(getApplication());
        userRepository = new UserRepository(getApplication());

        loadCurrentUser();

        btnBack.setOnClickListener(v -> finish());
        layoutDatePicker.setOnClickListener(v -> showMonthPicker());

        updateMonthDisplay();
        loadDataForSelectedMonth();
    }

    // --- HÀM HIỂN THỊ POPUP XÓA ---
    private void showDeleteDialog(Transaction transaction) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa giao dịch")
                .setMessage("Bạn có chắc muốn xóa giao dịch này? Số tiền sẽ được hoàn lại vào ví.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    repository.deleteTransaction(transaction);
                    Toast.makeText(this, "Đã xóa giao dịch", Toast.LENGTH_SHORT).show();
                    // Dữ liệu sẽ tự động cập nhật nhờ LiveData, không cần gọi loadData lại
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    // -----------------------------

    private void loadCurrentUser() {
        new Thread(() -> {
            User user = userRepository.getLoggedInUser();
            if (user != null) {
                currentUserId = user.id;
                runOnUiThread(this::loadDataForSelectedMonth);
            }
        }).start();
    }

    private void updateMonthDisplay() {
        String formattedDate = String.format("%02d-%d", selectedDate.getMonthValue(), selectedDate.getYear());
        tvCurrentMonthDisplay.setText(formattedDate);
    }

    private void loadDataForSelectedMonth() {
        repository.getTransactionsByMonth(currentUserId, selectedDate).observe(this, transactions -> {
            if (transactions != null) {
                adapter.setData(transactions);
            }
        });
    }

    private void showMonthPicker() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_month_picker, null);
        dialog.setContentView(view);

        ImageView btnPrevYear = view.findViewById(R.id.btn_prev_year);
        ImageView btnNextYear = view.findViewById(R.id.btn_next_year);
        TextView tvDialogYear = view.findViewById(R.id.tv_dialog_year);
        GridLayout gridMonths = view.findViewById(R.id.grid_months);

        final int[] dialogYear = {selectedDate.getYear()};
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
                loadDataForSelectedMonth();
                dialog.dismiss();
            });
            grid.addView(tv);
        }
    }
}