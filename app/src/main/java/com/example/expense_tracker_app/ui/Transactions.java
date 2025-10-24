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
import com.example.expense_tracker_app.ui.view.DonutChartView;

import java.time.LocalDate;
import java.util.*;

public class Transactions extends AppCompatActivity {

    private Map<String, List<CatSum>> monthlyData = new HashMap<>();
    private DonutChartView donutChart;
    private TextView tvTotalAmount, tvCenterTop;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transactions);

        // --- init views ---
        donutChart = findViewById(R.id.donutChart);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvCenterTop = findViewById(R.id.tvCenterTop);

        // --- mock data ---
        initMockData();

        // --- setup RecyclerView ---
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
            @Override public void onScrollStateChanged(@NonNull RecyclerView r, int state){
                if (state == RecyclerView.SCROLL_STATE_IDLE){
                    View v = snap.findSnapView(lm);
                    if (v == null) return;
                    int idx = lm.getPosition(v);
                    if (idx != RecyclerView.NO_POSITION && idx != adapter.selected){
                        adapter.selected = idx;
                        adapter.notifyDataSetChanged();
                        MonthItem cur = months.get(idx);
                        onMonthSelected(cur.year, cur.month);
                    }
                }
            }
        });


        // Transactions.java  (thêm vào onCreate)
        findViewById(R.id.item1_2023_03_04).setOnClickListener(v -> {
            Intent i = new Intent(Transactions.this, TransactionDetail.class);
            i.putExtra("amount", "- 99.000 đ");
            i.putExtra("category", "Ăn uống");
            i.putExtra("note", "Đi ăn mì cay");
            i.putExtra("date", "Ngày 16 tháng 03 năm 2022");
            i.putExtra("method", "Tiền mặt");
            i.putExtra("address", "123 Nguyễn Khánh Toàn, Cầu...");
            startActivity(i);
        });

    }

    // ---------------- MOCK DATA ----------------
    static class CatSum {
        String name;
        float amount;
        CatSum(String n, float a){name=n; amount=a;}
    }

    private void initMockData(){
        monthlyData.put("2025-03", Arrays.asList(
                new CatSum("Ăn uống", 1900000f),
                new CatSum("Cà phê", 450000f),
                new CatSum("Chi tiêu cố định", 3100000f),
                new CatSum("Khác", 780000f)
        ));
        monthlyData.put("2025-04", Arrays.asList(
                new CatSum("Ăn uống", 1700000f),
                new CatSum("Cà phê", 390000f),
                new CatSum("Giải trí", 1000000f)
        ));
        monthlyData.put("2025-05", Arrays.asList(
                new CatSum("Ăn uống", 2100000f),
                new CatSum("Cà phê", 520000f),
                new CatSum("Cố định", 2950000f),
                new CatSum("Khác", 880000f)
        ));
    }

    private void onMonthSelected(int year, int month){
        String key = String.format(Locale.US, "%04d-%02d", year, month);
        List<CatSum> cats = monthlyData.getOrDefault(key, Collections.emptyList());

        float total = 0f;
        for (CatSum c: cats) total += c.amount;

        // --- update total text ---
        tvTotalAmount.setText(formatVND(total));
        tvCenterTop.setText(total == 0 ? "0 đ" : formatVND(total));

        // --- update donut ---
        if (total <= 0f){
            donutChart.setValues(new float[]{0f});
            return;
        }
        float[] vals = new float[cats.size()];
        for (int i=0;i<cats.size();i++) vals[i] = cats.get(i).amount;
        donutChart.setValues(vals);
    }

    private String formatVND(float v){
        java.text.NumberFormat f = java.text.NumberFormat.getInstance(new Locale("vi","VN"));
        return f.format(Math.round(v)) + " đ";
    }

    // ---------------- UTIL ----------------
    private static List<MonthItem> buildMonths(int centerMonths){
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusMonths(centerMonths);
        List<MonthItem> list = new ArrayList<>();
        for(int i=0;i<=centerMonths*2;i++){
            LocalDate d = start.plusMonths(i);
            list.add(new MonthItem(d.getYear(), d.getMonthValue()));
        }
        return list;
    }

    private static int findCurrentIndex(List<MonthItem> list){
        LocalDate now = LocalDate.now();
        for (int i=0;i<list.size();i++){
            MonthItem it = list.get(i);
            if (it.year==now.getYear() && it.month==now.getMonthValue()) return i;
        }
        return list.size()/2;
    }
}
