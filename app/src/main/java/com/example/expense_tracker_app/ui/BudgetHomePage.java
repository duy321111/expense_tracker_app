package com.example.expense_tracker_app.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.ui.Month.MonthAdapter;
import com.example.expense_tracker_app.ui.Month.MonthItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class BudgetHomePage extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget_homepage);

        RecyclerView rvMonths = findViewById(R.id.rvMonths);
        setupMonthRecycler(rvMonths);
        LinearLayout budgetCardHome = findViewById(R.id.BudgetCardHome);
        budgetCardHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang Activity chi tiết ngân sách
                Intent intent = new Intent(BudgetHomePage.this, BudgetDetail.class);
                startActivity(intent);
            }

        });


        Button btnAddBudget = findViewById(R.id.btnAddBudget);
        btnAddBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang AddBudgetActivity
                Intent intent = new Intent(BudgetHomePage.this, AddBudget.class);
                startActivity(intent);
            }
        });
    }

    private void setupMonthRecycler(RecyclerView rv) {
        LinearLayoutManager lm = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        rv.setLayoutManager(lm);

        List<MonthItem> months = buildMonths(24);
        MonthAdapter adapter = new MonthAdapter(months);
        rv.setAdapter(adapter);
    }


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
}
