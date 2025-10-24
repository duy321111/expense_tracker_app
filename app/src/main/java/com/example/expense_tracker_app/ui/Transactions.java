package com.example.expense_tracker_app.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Transactions extends AppCompatActivity {
    @Override protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transactions);

        RecyclerView rv = findViewById(R.id.rvMonths);
        LinearLayoutManager lm = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        rv.setLayoutManager(lm);

        List<MonthItem> months = buildMonths(24); // 24 trước + 24 sau
        int currentIndex = findCurrentIndex(months);

        MonthAdapter adapter = new MonthAdapter(months);
        rv.setAdapter(adapter);

        PagerSnapHelper snap = new PagerSnapHelper();
        snap.attachToRecyclerView(rv);

        rv.post(() -> {
            adapter.selected = currentIndex;
            rv.scrollToPosition(currentIndex);
            adapter.notifyDataSetChanged();
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
                    }
                }
            }
        });
    }

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
