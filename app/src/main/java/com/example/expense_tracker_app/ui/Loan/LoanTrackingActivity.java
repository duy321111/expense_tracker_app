// LoanTrackingActivity.java
package com.example.expense_tracker_app.ui.Loan;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.model.DailyLoanSection;
import com.example.expense_tracker_app.model.LoanTransaction;
import com.example.expense_tracker_app.ui.Loan.DailySectionAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// dùng MonthAdapter của bạn
import com.example.expense_tracker_app.ui.Month.MonthAdapter;
import com.example.expense_tracker_app.ui.Month.MonthItem;

public class LoanTrackingActivity extends AppCompatActivity
        implements DailySectionAdapter.OnTransactionClickListener {

    private ImageView ivBack;
    private TextView tvYear;
    private RecyclerView rvMonthSelector;
    private RecyclerView rvLoanTransactions;
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabAdd;

    private MonthAdapter monthAdapter;
    private DailySectionAdapter dailySectionAdapter;

    private Calendar currentCalendar;
    private int selectedMonth;
    private int selectedYear;

    private final List<MonthItem> monthItems = new ArrayList<>();
    private int lastObservedSelected = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_tracking);

        initViews();
        setupCalendar();
        setupMonthSelector();
        setupTransactionsList();
        setupClickListeners();
        loadDataForSelectedMonth();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvYear = findViewById(R.id.tvYear);
        rvMonthSelector = findViewById(R.id.rvMonthSelector);
        rvLoanTransactions = findViewById(R.id.rvLoanTransactions);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabAdd = findViewById(R.id.fabAdd);
    }

    private void setupCalendar() {
        currentCalendar = Calendar.getInstance();
        selectedMonth = currentCalendar.get(Calendar.MONTH) + 1;
        selectedYear = currentCalendar.get(Calendar.YEAR);
        tvYear.setText(String.valueOf(selectedYear));
    }

    private void setupMonthSelector() {
        monthItems.clear();
        for (int i = 1; i <= 12; i++) monthItems.add(new MonthItem(i, selectedYear));

        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvMonthSelector.setLayoutManager(lm);

        monthAdapter = new MonthAdapter(monthItems);
        rvMonthSelector.setAdapter(monthAdapter);

        // chọn tháng hiện tại
        int initialIndex = selectedMonth - 1;
        monthAdapter.selected = initialIndex;
        lastObservedSelected = initialIndex;
        rvMonthSelector.scrollToPosition(initialIndex);

        // Snap
        new PagerSnapHelper().attachToRecyclerView(rvMonthSelector);

        // Quan sát thay đổi selection qua notifyItemChanged của Adapter
        monthAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override public void onItemRangeChanged(int positionStart, int itemCount) {
                handleSelectionIfChanged();
            }
            @Override public void onChanged() {
                handleSelectionIfChanged();
            }
        });

        // Load thêm tháng khi scroll tới đầu/cuối
        rvMonthSelector.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (llm == null) return;
                int firstVisible = llm.findFirstVisibleItemPosition();
                int lastVisible = llm.findLastVisibleItemPosition();

                if (firstVisible == 0 && dx < 0) loadPreviousYearMonths();
                if (lastVisible == monthItems.size() - 1 && dx > 0) loadNextYearMonths();
            }
        });
    }

    private void handleSelectionIfChanged() {
        if (monthAdapter == null) return;
        int idx = monthAdapter.selected;
        if (idx == RecyclerView.NO_POSITION || idx == lastObservedSelected) return;

        lastObservedSelected = idx;
        MonthItem mi = monthItems.get(idx);
        selectedMonth = mi.month;
        selectedYear = mi.year;
        tvYear.setText(String.valueOf(selectedYear));
        loadDataForSelectedMonth();
    }

    private void setupTransactionsList() {
        rvLoanTransactions.setLayoutManager(new LinearLayoutManager(this));
        dailySectionAdapter = new DailySectionAdapter(this);
        rvLoanTransactions.setAdapter(dailySectionAdapter);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        fabAdd.setOnClickListener(v ->
                Toast.makeText(this, "Thêm giao dịch mới", Toast.LENGTH_SHORT).show());

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) { Toast.makeText(this, "Trang chủ", Toast.LENGTH_SHORT).show(); return true; }
            if (id == R.id.nav_transactions) { Toast.makeText(this, "Giao dịch", Toast.LENGTH_SHORT).show(); return true; }
            if (id == R.id.nav_add) { return true; }
            if (id == R.id.nav_reports) { Toast.makeText(this, "Báo cáo", Toast.LENGTH_SHORT).show(); return true; }
            if (id == R.id.nav_profile) { Toast.makeText(this, "Cá nhân", Toast.LENGTH_SHORT).show(); return true; }
            return false;
        });
    }

    @Override
    public void onTransactionClick(LoanTransaction transaction) {
        Toast.makeText(this, "Chi tiết: " + transaction.getPersonName(), Toast.LENGTH_SHORT).show();
    }

    private void loadDataForSelectedMonth() {
        List<DailyLoanSection> sections = generateMockData(selectedMonth, selectedYear);
        dailySectionAdapter.setSections(sections);
    }

    private void loadPreviousYearMonths() {
        int firstYear = monthItems.get(0).year;
        int previousYear = firstYear - 1;
        List<MonthItem> newMonths = new ArrayList<>();
        for (int i = 1; i <= 12; i++) newMonths.add(new MonthItem(i, previousYear));
        monthItems.addAll(0, newMonths);
        monthAdapter.notifyItemRangeInserted(0, 12);
    }

    private void loadNextYearMonths() {
        int lastYear = monthItems.get(monthItems.size() - 1).year;
        int nextYear = lastYear + 1;
        int start = monthItems.size();
        for (int i = 1; i <= 12; i++) monthItems.add(new MonthItem(i, nextYear));
        monthAdapter.notifyItemRangeInserted(start, 12);
    }

    // giữ nguyên mock data của bạn
    private List<DailyLoanSection> generateMockData(int month, int year) {
        List<DailyLoanSection> sections = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        cal.set(year, month - 1, 8);
        DailyLoanSection s1 = new DailyLoanSection(cal.getTime());
        s1.addTransaction(new LoanTransaction("1","borrow","Home Credit",5000000,cal.getTime(),"active", R.color.success_1));
        s1.addTransaction(new LoanTransaction("2","lend","Nguyễn Đức Thọ",3000000,cal.getTime(),"active", R.color.success_1));
        sections.add(s1);

        cal.set(year, month - 1, 12);
        DailyLoanSection s2 = new DailyLoanSection(cal.getTime());
        s2.addTransaction(new LoanTransaction("3","lend","Trần Trung Hiếu",2000000,cal.getTime(),"active", R.color.success_1));
        s2.addTransaction(new LoanTransaction("4","borrow","FE Credit",10000000,cal.getTime(),"active", R.color.success_1));
        sections.add(s2);

        return sections;
    }
}
