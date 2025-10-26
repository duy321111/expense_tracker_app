package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.ui.Month.MonthAdapter;
import com.example.expense_tracker_app.ui.Month.MonthItem;
import com.google.android.material.appbar.MaterialToolbar;

import java.time.LocalDate;
import java.util.*;

public class Transactions extends AppCompatActivity {

    private final Map<String, List<CatSum>> monthlyData = new HashMap<>();
    private DonutChartView donutChart;
    private TextView tvTotalAmount, tvCenterTop;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transactions);

        // Toolbar back
        MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> onBackPressed()); // hoặc finish()

        donutChart = findViewById(R.id.donutChart);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvCenterTop = findViewById(R.id.tvCenterTop);

        initMockData();

        // month strip
        RecyclerView rv = findViewById(R.id.rvMonths);
        LinearLayoutManager lm = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        rv.setLayoutManager(lm);

        List<MonthItem> months = buildMonths(24);
        int currentIndex = findCurrentIndex(months);

        MonthAdapter adapter = new MonthAdapter(months);
        rv.setAdapter(adapter);

        PagerSnapHelper snap = new PagerSnapHelper();
        snap.attachToRecyclerView(rv);

        rv.post(() -> {
            adapter.selected = currentIndex;
            rv.scrollToPosition(currentIndex);
            adapter.notifyDataSetChanged();
            MonthItem cur = months.get(currentIndex);
            onMonthSelected(cur.year, cur.month);
        });

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView r, int state) {
                if (state == RecyclerView.SCROLL_STATE_IDLE) {
                    View v = snap.findSnapView(lm);
                    if (v == null) return;
                    int idx = lm.getPosition(v);
                    if (idx != RecyclerView.NO_POSITION && idx != adapter.selected) {
                        adapter.selected = idx;
                        adapter.notifyDataSetChanged();
                        MonthItem cur = months.get(idx);
                        onMonthSelected(cur.year, cur.month);
                    }
                }
            }
        });

        // ===== mở chi tiết khi bấm vào item =====
        // Ăn uống
        findViewById(R.id.item1_2023_03_04).setOnClickListener(v ->
                openDetail("- 99.000 đ", "Ăn uống", "Đi ăn mì cay",
                        "Ngày 04 tháng 03 2023", "Tiền mặt",
                        "123 Nguyễn Khánh Toàn, Cầu..."));

        // Cà phê
        findViewById(R.id.item2_2023_03_04).setOnClickListener(v ->
                openDetail("- 150.000 đ", "Cà phê", "Uống cà phê sáng",
                        "Ngày 04 tháng 03 2023", "Chuyển khoản",
                        "Quán Coffee XYZ, Q.1"));

        // Trả nợ
        findViewById(R.id.item3_2023_03_04).setOnClickListener(v ->
                openDetail("- 420.000 đ", "Trả nợ", "Trả nợ bạn A",
                        "Ngày 04 tháng 03 2023", "Chuyển khoản",
                        "Ngân hàng ABC"));
    }

    private void openDetail(String amount, String category, String note,
                            String date, String method, String address) {
        Intent i = new Intent(Transactions.this, TransactionDetail.class);
        i.putExtra("amount", amount);
        i.putExtra("category", category);
        i.putExtra("note", note);
        i.putExtra("date", date);
        i.putExtra("method", method);
        i.putExtra("address", address);
        startActivity(i);
    }

    // ---------------- MOCK DATA ----------------
    static class CatSum {
        String name;
        float amount;
        CatSum(String n, float a) { name = n; amount = a; }
    }

    private void initMockData() {
        monthlyData.put("2025-03", Arrays.asList(
                new CatSum("Ăn uống", 1_900_000f),
                new CatSum("Cà phê", 450_000f),
                new CatSum("Chi tiêu cố định", 3_100_000f),
                new CatSum("Khác", 780_000f)
        ));
        monthlyData.put("2025-04", Arrays.asList(
                new CatSum("Ăn uống", 1_700_000f),
                new CatSum("Cà phê", 390_000f),
                new CatSum("Giải trí", 1_000_000f)
        ));
        monthlyData.put("2025-05", Arrays.asList(
                new CatSum("Ăn uống", 2_100_000f),
                new CatSum("Cà phê", 520_000f),
                new CatSum("Cố định", 2_950_000f),
                new CatSum("Khác", 880_000f)
        ));
    }

    private void onMonthSelected(int year, int month) {
        String key = String.format(Locale.US, "%04d-%02d", year, month);
        List<CatSum> cats = monthlyData.getOrDefault(key, Collections.emptyList());

        float total = 0f;
        for (CatSum c : cats) total += c.amount;

        tvTotalAmount.setText(formatVND(total));
        tvCenterTop.setText(total == 0 ? "0 đ" : formatVND(total));

        if (total <= 0f) {
            donutChart.setValues(new float[]{0f});
            return;
        }
        float[] vals = new float[cats.size()];
        for (int i = 0; i < cats.size(); i++) vals[i] = cats.get(i).amount;
        donutChart.setValues(vals);
    }

    private String formatVND(float v) {
        java.text.NumberFormat f = java.text.NumberFormat.getInstance(new Locale("vi", "VN"));
        return f.format(Math.round(v)) + " đ";
    }

    // ---------------- UTIL ----------------
    private static List<MonthItem> buildMonths(int centerMonths) {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusMonths(centerMonths);
        List<MonthItem> list = new ArrayList<>();
        for (int i = 0; i <= centerMonths * 2; i++) {
            LocalDate d = start.plusMonths(i);
            list.add(new MonthItem(d.getYear(), d.getMonthValue()));
        }
        return list;
    }

    private static int findCurrentIndex(List<MonthItem> list) {
        LocalDate now = LocalDate.now();
        for (int i = 0; i < list.size(); i++) {
            MonthItem it = list.get(i);
            if (it.year == now.getYear() && it.month == now.getMonthValue()) return i;
        }
        return list.size() / 2;
    }
}
