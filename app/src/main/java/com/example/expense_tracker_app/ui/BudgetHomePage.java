package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.ui.Month.MonthAdapter;
import com.example.expense_tracker_app.ui.Month.MonthItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BudgetHomePage extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.budget_homepage, container, false);

        RecyclerView rvMonths = root.findViewById(R.id.rvMonths);
        setupMonthRecycler(rvMonths);

        LinearLayout budgetCardHome = root.findViewById(R.id.BudgetCardHome);
        budgetCardHome.setOnClickListener(v -> {
            // Chuyển sang Activity chi tiết ngân sách
            Intent intent = new Intent(getActivity(), BudgetDetail.class);
            startActivity(intent);
        });

        Button btnAddBudget = root.findViewById(R.id.btnAddBudget);
        btnAddBudget.setOnClickListener(v -> {
            // Chuyển sang AddBudgetActivity
            Intent intent = new Intent(getActivity(), AddBudget.class);
            startActivity(intent);
        });

        return root;
    }

    private void setupMonthRecycler(RecyclerView rv) {
        LinearLayoutManager lm = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
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
