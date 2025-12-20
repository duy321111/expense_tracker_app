package com.example.expense_tracker_app.ui;

import android.os.Bundle;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense_tracker_app.ui.adapter.TransactionAdapter;
import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.repository.TransactionRepository;

import java.time.LocalDate;

public class TransactionHistoryFragment extends AppCompatActivity {

    private RecyclerView rvTransactions;
    private ImageView btnBack;
    private LinearLayout layoutMonthContainer;
    private HorizontalScrollView scrollMonths;

    private TransactionAdapter adapter;
    private TransactionRepository repository;

    // Biến lưu tháng đang chọn (Mặc định là hôm nay)
    private LocalDate selectedDate = LocalDate.now();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_transaction_history);

        // 1. Ánh xạ View
        rvTransactions = findViewById(R.id.rv_transactions);
        btnBack = findViewById(R.id.btn_back);
        layoutMonthContainer = findViewById(R.id.layout_month_container); // ID mới thêm trong XML
        scrollMonths = findViewById(R.id.scroll_months);

        // 2. Setup RecyclerView
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this);
        rvTransactions.setAdapter(adapter);

        // 3. Khởi tạo Repository
        repository = new TransactionRepository(getApplication());

        // 4. Xử lý sự kiện
        btnBack.setOnClickListener(v -> finish());

        // 5. Tạo danh sách tháng và tải dữ liệu
        setupMonthTabs();
        loadDataForSelectedMonth();
    }

    private void setupMonthTabs() {
        layoutMonthContainer.removeAllViews(); // Xóa sạch views cũ nếu có

        int currentMonth = selectedDate.getMonthValue(); // Tháng hiện tại (ví dụ 12)
        int currentYear = selectedDate.getYear();

        // Vòng lặp tạo 12 tháng
        for (int i = 1; i <= 12; i++) {
            TextView tvMonth = new TextView(this);
            tvMonth.setText("Tháng " + i);
            tvMonth.setPadding(32, 16, 32, 16);
            tvMonth.setTextSize(14);

            // Layout params để có khoảng cách
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            tvMonth.setLayoutParams(params);

            // Kiểm tra xem có phải tháng đang chọn không
            if (i == currentMonth) {
                // Style cho tháng ĐƯỢC CHỌN (Đậm, màu đen)
                tvMonth.setTextColor(getColor(R.color.neutral_900)); // Màu đen
                tvMonth.setTypeface(ResourcesCompat.getFont(this, R.font.bold)); // Font đậm
                // Nếu bạn có drawable gạch chân dưới thì setBackground ở đây
            } else {
                // Style cho tháng THƯỜNG (Nhạt, thường)
                tvMonth.setTextColor(getColor(R.color.neutral_500)); // Màu xám
                tvMonth.setTypeface(ResourcesCompat.getFont(this, R.font.regular));
            }

            final int monthToLoad = i;
            tvMonth.setOnClickListener(v -> {
                // Cập nhật ngày đang chọn
                selectedDate = LocalDate.of(currentYear, monthToLoad, 1);
                // Vẽ lại danh sách tháng (để cập nhật màu đậm/nhạt)
                setupMonthTabs();
                // Tải lại dữ liệu
                loadDataForSelectedMonth();
            });

            layoutMonthContainer.addView(tvMonth);

            // Tự động cuộn tới tháng hiện tại lúc mở app
            if (i == currentMonth) {
                scrollMonths.post(() -> scrollMonths.smoothScrollTo(tvMonth.getLeft(), 0));
            }
        }
    }

    private void loadDataForSelectedMonth() {
        // Gọi repository lấy dữ liệu theo tháng đang chọn
        // userId = 1 (Ví dụ)
        repository.getTransactionsByMonth(1, selectedDate).observe(this, transactions -> {
            if (transactions != null) {
                adapter.setData(transactions);
            }
        });
    }
}