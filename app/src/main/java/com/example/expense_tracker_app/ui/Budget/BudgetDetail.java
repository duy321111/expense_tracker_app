package com.example.expense_tracker_app.ui.Budget;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.TxType;
import com.example.expense_tracker_app.data.repository.TransactionRepository;
import com.example.expense_tracker_app.data.repository.BudgetRepository;
import com.example.expense_tracker_app.data.model.Budget;
import com.example.expense_tracker_app.ui.adapter.TransactionAdapter; // Import Adapter mới

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetDetail extends AppCompatActivity {
    @Override
    protected void onResume() {
        super.onResume();
        // Luôn refresh dữ liệu khi quay lại màn hình này
        bindDataFromIntentAndLoadTransactions();
    }

    private TextView tvTotalAmount, tvCenterTop;
    private TextView tvMonthYear, tvBudgetName;

    // 1. Thay thế LinearLayout cũ bằng RecyclerView
    private RecyclerView rvBudgetList;
    private TransactionAdapter adapter;

    private TransactionRepository transactionRepository;
    private BudgetRepository budgetRepository;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    private static final int REQUEST_EDIT_BUDGET = 1001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget_detail);
        transactionRepository = new TransactionRepository(getApplication());
        budgetRepository = new BudgetRepository(this);

        // --- Toolbar ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết ngân sách");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // --- Init Views ---
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvCenterTop = findViewById(R.id.tvCenterTop);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvBudgetName = findViewById(R.id.tvBudgetName);

        // 2. Cấu hình RecyclerView
        rvBudgetList = findViewById(R.id.rvBudgetList); // Đảm bảo ID này khớp với file XML layout
        rvBudgetList.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo Adapter
        adapter = new TransactionAdapter(this);
        rvBudgetList.setAdapter(adapter);

        // Populate dữ liệu
        bindDataFromIntentAndLoadTransactions();

        // TextView "Sửa"
        TextView tvEdit = findViewById(R.id.tvEdit);
        tvEdit.setOnClickListener(v -> {
            Intent intent = new Intent(BudgetDetail.this, EditBudget.class);
            if (getIntent() != null) {
                int budgetId = getIntent().getIntExtra("budget_id", -1);
                if (budgetId > 0) {
                    intent.putExtra("budget_id", budgetId);
                }
            }
            startActivityForResult(intent, REQUEST_EDIT_BUDGET);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_BUDGET && resultCode == RESULT_OK) {
            // Sau khi sửa ngân sách xong, reload lại dữ liệu
            bindDataFromIntentAndLoadTransactions();
        }
    }

    private void bindDataFromIntentAndLoadTransactions() {
        if (getIntent() == null) return;

        int budgetId = getIntent().getIntExtra("budget_id", -1);
        if (budgetId <= 0) return;

        Budget budget = budgetRepository.getBudgetById(budgetId);
        if (budget == null) return;

        tvBudgetName.setText(budget.getName());
        double limit = budget.getAmount();
        int month = budget.getMonth();
        int year = budget.getYear();
        List<Integer> subcategoryIds = budget.getSubcategoryIds();

        // Tính tổng đã chi thực tế từ transaction
        double spent = 0;
        if (subcategoryIds != null && !subcategoryIds.isEmpty() && month > 0 && year > 0) {
            java.time.LocalDate firstDayOfMonth = java.time.LocalDate.of(year, month, 1);
            java.time.LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
            long startEpochDay = firstDayOfMonth.toEpochDay();
            long endEpochDay = lastDayOfMonth.toEpochDay();
            List<Transaction> allTxs = transactionRepository.getTransactionsBySubcategories(1, startEpochDay, endEpochDay, subcategoryIds);
            for (Transaction t : allTxs) {
                if (t.type == TxType.EXPENSE) spent += t.amount;
            }
        }
        // Hiển thị tổng tiền
        String totalText = formatCurrency(spent) + " / " + formatCurrency(limit);
        tvTotalAmount.setText(totalText);
        // Nếu vượt quá hạn mức thì tô đỏ, bình thường để xanh lá (success_1)
        if (limit > 0 && spent > limit) {
            tvTotalAmount.setTextColor(getResources().getColor(R.color.error_1));
        } else {
            tvTotalAmount.setTextColor(getResources().getColor(R.color.success_1));
        }

        // Hiển thị phần trăm (có thể lớn hơn 100%)
        int percent = limit <= 0 ? 0 : (int) Math.round((spent / limit) * 100);
        tvCenterTop.setText(percent + "%");
        // Nếu vượt quá 100% thì tô đỏ
        if (limit > 0 && spent > limit) {
            tvCenterTop.setTextColor(getResources().getColor(R.color.error_1));
        } else {
            tvCenterTop.setTextColor(getResources().getColor(R.color.primary_1));
        }

        if (month > 0 && year > 0) {
            tvMonthYear.setText("Tháng " + month + " năm " + year);
        }

        // Load giao dịch
        loadTransactionsForBudget(month, year, budget.getName());
    }

    private void loadTransactionsForBudget(int month, int year, String budgetName) {
        android.util.Log.d("BudgetDetail", "loadTransactionsForBudget: month=" + month + ", year=" + year + ", budgetName=" + budgetName);
        if (month <= 0 || year <= 0) return;

        // Lấy list subcategoryId từ Intent (giả sử truyền vào dưới dạng ArrayList<Integer> với key "subcategory_ids")
        List<Integer> subcategoryIds = getIntent().getIntegerArrayListExtra("subcategory_ids");
        android.util.Log.d("BudgetDetail", "subcategoryIds=" + (subcategoryIds != null ? subcategoryIds.toString() : "null"));
        if (subcategoryIds == null || subcategoryIds.isEmpty()) {
            adapter.setData(new ArrayList<>());
            android.util.Log.d("BudgetDetail", "No subcategoryIds, returning empty list");
            return;
        }

        // Lấy giao dịch theo subcategory id, trong khoảng từ đầu tháng đến cuối tháng
        java.time.LocalDate firstDayOfMonth = java.time.LocalDate.of(year, month, 1);
        java.time.LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
        long startEpochDay = firstDayOfMonth.toEpochDay();
        long endEpochDay = lastDayOfMonth.toEpochDay();
        android.util.Log.d("BudgetDetail", "startEpochDay=" + startEpochDay + ", endEpochDay=" + endEpochDay);
        List<Transaction> allTxs = transactionRepository.getTransactionsBySubcategories(1, startEpochDay, endEpochDay, subcategoryIds);
        android.util.Log.d("BudgetDetail", "allTxs.size=" + (allTxs != null ? allTxs.size() : 0));

        // Lọc chỉ lấy EXPENSE
        List<Transaction> filteredList = new ArrayList<>();
        for (Transaction t : allTxs) {
            if (t.type == TxType.EXPENSE) {
                filteredList.add(t);
            }
        }
        android.util.Log.d("BudgetDetail", "filteredList.size (EXPENSE)=" + filteredList.size());
        adapter.setData(filteredList);
    }

    private String formatCurrency(double value) {
        return currencyFormat.format(Math.round(value)).replace("₫", "đ");
    }


}