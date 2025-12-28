package com.example.expense_tracker_app.ui;

import android.app.AlertDialog;
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
import androidx.lifecycle.LiveData; // Import thêm cái này
import androidx.lifecycle.Observer; // Import thêm cái này
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
import java.util.List;

public class Transactions extends AppCompatActivity {

    private RecyclerView rvTransactions;
    private ImageView btnBack;
    // private LinearLayout layoutDatePicker;
    // private TextView tvCurrentMonthDisplay;

    private TransactionAdapter adapter;
    private TransactionRepository repository;
    private UserRepository userRepository;

    private final LocalDate selectedDate = LocalDate.now();
    private int currentUserId = 1; // Mặc định là 1

    // --- BIẾN QUẢN LÝ OBSERVER ĐỂ TRÁNH LỖI CHỒNG CHÉO ---
    private LiveData<List<Transaction>> currentLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transactions);

        rvTransactions = findViewById(R.id.rv_transactions);
        btnBack = findViewById(R.id.btn_back);
        // layoutDatePicker = findViewById(R.id.layout_date_picker);
        // tvCurrentMonthDisplay = findViewById(R.id.tv_current_month_display);

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TransactionAdapter(this, this::showDeleteDialog);
        rvTransactions.setAdapter(adapter);

        repository = new TransactionRepository(getApplication());
        userRepository = new UserRepository(getApplication());

        btnBack.setOnClickListener(v -> finish());
        // layoutDatePicker.setOnClickListener(v -> showMonthPicker());

        // updateMonthDisplay();

        // SỬA: Không gọi loadData ở đây nữa, mà đợi loadUser xong mới gọi
        loadCurrentUser();
    }

    private void showDeleteDialog(Transaction transaction) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa giao dịch")
                .setMessage("Bạn có chắc muốn xóa giao dịch này? Số tiền sẽ được hoàn lại vào ví.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    repository.deleteTransaction(transaction);
                    Toast.makeText(this, "Đã xóa giao dịch", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadCurrentUser() {
        new Thread(() -> {
            User user = userRepository.getLoggedInUser();
            if (user != null) {
                currentUserId = user.id;
            }
            // Dù có user hay không cũng phải load dữ liệu (trên UI Thread)
            runOnUiThread(this::loadDataForSelectedMonth);
        }).start();
    }

    // private void updateMonthDisplay() {}

    // --- SỬA: Hàm load dữ liệu xử lý thông minh hơn ---
    private void loadDataForSelectedMonth() {
        // 1. Hủy theo dõi (Observer) cũ nếu có, để tránh bị double dữ liệu
        if (currentLiveData != null) {
            currentLiveData.removeObservers(this);
        }

        // 2. Lấy LiveData mới theo userId và ngày đã chọn
        currentLiveData = repository.getTransactionsByMonth(currentUserId, LocalDate.now());

        // 3. Đăng ký theo dõi mới
        currentLiveData.observe(this, transactions -> {
            if (transactions != null) {
                adapter.setData(transactions);
                // Nếu muốn hiện thông báo khi trống:
                // if (transactions.isEmpty()) { ... hiển thị text trống ... }
            }
        });
    }
    // --------------------------------------------------

    // private void showMonthPicker() {}
    // private void renderMonthGrid(BottomSheetDialog dialog, GridLayout grid, int year) {}
}