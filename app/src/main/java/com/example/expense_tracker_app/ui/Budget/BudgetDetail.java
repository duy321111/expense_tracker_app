package com.example.expense_tracker_app.ui.Budget;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.TransactionItem;
import com.example.expense_tracker_app.data.model.TxType;
import com.example.expense_tracker_app.data.repository.InMemoryRepo;
import com.example.expense_tracker_app.data.repository.Repository;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetDetail extends AppCompatActivity {

    private TextView tvTotalAmount, tvCenterTop;
    private TextView tvMonthYear, tvBudgetName;
    private LinearLayout transactionListLayout;
    private final List<TransactionItem> transactionItems = new ArrayList<>();

    private final Repository repo = InMemoryRepo.get();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

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
        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvBudgetName = findViewById(R.id.tvBudgetName);
        transactionListLayout = findViewById(R.id.transactionList);

        // Populate từ Intent và load giao dịch
        bindDataFromIntentAndLoadTransactions();

        // TextView "Sửa"
        TextView tvEdit = findViewById(R.id.tvEdit);
        tvEdit.setOnClickListener(v -> {
            Intent intent = new Intent(BudgetDetail.this, EditBudget.class);
            // Truyền budget_id để EditBudget load dữ liệu
            if (getIntent() != null) {
                int budgetId = getIntent().getIntExtra("budget_id", -1);
                if (budgetId > 0) {
                    intent.putExtra("budget_id", budgetId);
                }
            }
            startActivity(intent);
        });
    }

    private void bindDataFromIntentAndLoadTransactions() {
        if (getIntent() == null) return;

        String name = getIntent().getStringExtra("budget_name");
        double limit = getIntent().getDoubleExtra("budget_limit", 0);
        double spent = getIntent().getDoubleExtra("budget_spent", 0);
        int month = getIntent().getIntExtra("budget_month", 0);
        int year = getIntent().getIntExtra("budget_year", 0);

        // Tiêu đề toolbar + text trên card là tên ngân sách
        if (getSupportActionBar() != null) {
            // Tiêu đề trang chi tiết luôn là "Chi tiết ngân sách"
            getSupportActionBar().setTitle("Chi tiết ngân sách");
        }
        if (name != null) {
            tvBudgetName.setText(name);
        }

        // Tổng đã chi hiển thị rõ ràng
        String totalText = formatCurrency(spent) + " / " + formatCurrency(limit);
        tvTotalAmount.setText(totalText);

        // Phần trăm đã chi
        int percent = limit <= 0 ? 0 : (int) Math.min(100, Math.round((spent / limit) * 100));
        tvCenterTop.setText(percent + "%");

        if (month > 0 && year > 0) {
            tvMonthYear.setText("Tháng " + month + " năm " + year);
        }

        // Load giao dịch thực: theo tháng/năm (demo chưa filter thêm category)
        loadTransactionsForBudget(month, year, name);
    }

    private void loadTransactionsForBudget(int month, int year, String budgetName) {
        if (month <= 0 || year <= 0) return;

        // Ở đây demo: lấy tất cả giao dịch chi tiêu của tháng/năm, chưa filter theo category ngân sách
        List<Transaction> txs = repo.transactionsByMonth(year, month);
        transactionItems.clear();
        for (Transaction t : txs) {
            // Chỉ lấy chi tiêu
            if (t.type != TxType.EXPENSE) continue;

            String catName = t.category != null ? t.category.name : "Khác";
            String method = t.method;
            String amountStr = "- " + formatCurrency(t.amount);

            // TODO: map icon theo category nếu cần, tạm thời icon mặc định
            int iconRes = R.drawable.ic_cat_food;

            transactionItems.add(new TransactionItem(
                    catName,
                    method,
                    amountStr,
                    iconRes,
                    true
            ));
        }
        renderTransactions();
    }

    private String formatCurrency(double value) {
        return currencyFormat.format(Math.round(value)).replace("₫", "đ");
    }

    private void renderTransactions() {
        if (transactionListLayout == null) return;
        transactionListLayout.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        for (TransactionItem item : transactionItems) {
            android.view.View row = inflater.inflate(R.layout.item_transaction, transactionListLayout, false);

            TextView tvCategoryName = row.findViewById(R.id.tv_category_name);
            TextView tvPaymentMethod = row.findViewById(R.id.tv_payment_method);
            TextView tvAmount = row.findViewById(R.id.tv_amount);
            ImageView ivIcon = row.findViewById(R.id.img_category_icon);

            tvCategoryName.setText(item.getCategoryName());
            tvPaymentMethod.setText(item.getPaymentMethod());
            tvAmount.setText(item.getAmount());
            ivIcon.setImageResource(item.getIconResId());

            transactionListLayout.addView(row);
        }
    }
}
