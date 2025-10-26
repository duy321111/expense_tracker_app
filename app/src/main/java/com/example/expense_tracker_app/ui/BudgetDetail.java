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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetDetail extends AppCompatActivity {

    private Map<String, List<CatSum>> monthlyData = new HashMap<>();
    private DonutChartView donutChart;
    private TextView tvTotalAmount, tvCenterTop;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget_detail);

        // --- init views ---
        donutChart = findViewById(R.id.donutChart);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvCenterTop = findViewById(R.id.tvCenterTop);


        // --- setup RecyclerView ---




    }

    // ---------------- MOCK DATA ----------------
    static class CatSum {
        String name;
        float amount;

        CatSum(String n, float a) {
            name = n;
            amount = a;
        }
    }



    private void onMonthSelected(int year, int month) {
        String key = String.format(Locale.US, "%04d-%02d", year, month);
        List<CatSum> cats = monthlyData.getOrDefault(key, Collections.emptyList());

        float total = 0f;
        for (CatSum c : cats) total += c.amount;

        // --- update total text ---
        tvTotalAmount.setText(formatVND(total));
        tvCenterTop.setText(total == 0 ? "0 đ" : formatVND(total));

        // --- update donut ---
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
