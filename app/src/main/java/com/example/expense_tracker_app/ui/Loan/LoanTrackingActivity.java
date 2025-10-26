// LoanTrackingActivity.java
package com.example.expense_tracker_app.ui.Loan;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.model.DailyLoanSection;
import com.example.expense_tracker_app.model.LoanTransaction;
import com.example.expense_tracker_app.ui.Month.MonthAdapter;
import com.example.expense_tracker_app.ui.Month.MonthItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class LoanTrackingActivity extends AppCompatActivity
        implements DailySectionAdapter.OnTransactionClickListener {

    private ImageView ivBack;
    private RecyclerView rvMonths, rvDailySections;
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabAdd;

    private MonthAdapter monthAdapter;
    private DailySectionAdapter dailySectionAdapter;

    private int selectedMonth;
    private int selectedYear;

    private final List<MonthItem> monthItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_tracking);

        initViews();
        initCalendar();
        setupMonthStrip();
        setupDailySectionList();
        setupClicks();
        loadDataForSelectedMonth();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        rvMonths = findViewById(R.id.rvMonths);
        rvDailySections = findViewById(R.id.rvDailySections);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabAdd = findViewById(R.id.fabAdd);
    }

    private void initCalendar() {
        Calendar c = Calendar.getInstance();
        selectedMonth = c.get(Calendar.MONTH) + 1;
        selectedYear = c.get(Calendar.YEAR);
    }

    private void setupMonthStrip() {
        monthItems.clear();
        monthItems.addAll(buildMonths(24));

        LinearLayoutManager lm = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        rvMonths.setLayoutManager(lm);

        monthAdapter = new MonthAdapter(monthItems);
        rvMonths.setAdapter(monthAdapter);

        PagerSnapHelper snap = new PagerSnapHelper();
        snap.attachToRecyclerView(rvMonths);

        int currentIndex = findCurrentIndex(monthItems);

        rvMonths.post(() -> {
            monthAdapter.selected = currentIndex;
            rvMonths.scrollToPosition(currentIndex);
            monthAdapter.notifyDataSetChanged();
            MonthItem cur = monthItems.get(currentIndex);
            onMonthSelected(cur.year, cur.month);
        });

        rvMonths.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView r, int state) {
                if (state == RecyclerView.SCROLL_STATE_IDLE) {
                    View v = snap.findSnapView(lm);
                    if (v == null) return;
                    int idx = lm.getPosition(v);
                    if (idx != RecyclerView.NO_POSITION && idx != monthAdapter.selected) {
                        monthAdapter.selected = idx;
                        monthAdapter.notifyDataSetChanged();
                        MonthItem cur = monthItems.get(idx);
                        onMonthSelected(cur.year, cur.month);
                    }
                }
            }
        });
    }

    private void setupDailySectionList() {
        rvDailySections.setLayoutManager(new LinearLayoutManager(this));
        dailySectionAdapter = new DailySectionAdapter(this);
        rvDailySections.setAdapter(dailySectionAdapter);
    }

    private void setupClicks() {
        ivBack.setOnClickListener(v -> finish());

        fabAdd.setOnClickListener(v ->
                Toast.makeText(this, "Thêm giao dịch mới", Toast.LENGTH_SHORT).show());

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) { Toast.makeText(this, "Tổng quan", Toast.LENGTH_SHORT).show(); return true; }
            if (id == R.id.nav_reports) { Toast.makeText(this, "Thống kê", Toast.LENGTH_SHORT).show(); return true; }
            if (id == R.id.nav_budget) { Toast.makeText(this, "Ngân sách", Toast.LENGTH_SHORT).show(); return true; }
            if (id == R.id.nav_profile) { Toast.makeText(this, "Cá nhân", Toast.LENGTH_SHORT).show(); return true; }
            return false;
        });
    }

    private List<MonthItem> buildMonths(int centerMonths) {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusMonths(centerMonths);
        List<MonthItem> list = new ArrayList<>();
        for (int i = 0; i <= centerMonths * 2; i++) {
            LocalDate d = start.plusMonths(i);
            list.add(new MonthItem(d.getYear(), d.getMonthValue()));
        }
        return list;
    }

    private int findCurrentIndex(List<MonthItem> list) {
        LocalDate now = LocalDate.now();
        for (int i = 0; i < list.size(); i++) {
            MonthItem it = list.get(i);
            if (it.year == now.getYear() && it.month == now.getMonthValue()) return i;
        }
        return list.size() / 2;
    }

    private void onMonthSelected(int year, int month) {
        selectedYear = year;
        selectedMonth = month;
        loadDataForSelectedMonth();
    }

    private void loadDataForSelectedMonth() {
        List<DailyLoanSection> sections = generateMockData(selectedMonth, selectedYear);

        // Sắp xếp giảm dần theo ngày: mới nhất ở trên
        Collections.sort(sections, (a, b) -> b.getDate().compareTo(a.getDate()));

        dailySectionAdapter.setSections(sections);
        rvDailySections.scrollToPosition(0);
    }

    // Mock demo
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

    @Override
    public void onTransactionClick(LoanTransaction transaction) {
        Toast.makeText(this, "Chi tiết: " + transaction.getPersonName(), Toast.LENGTH_SHORT).show();
    }
}
