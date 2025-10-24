package com.example.expense_tracker_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.text.DateFormatSymbols;
import java.util.Calendar;

public class BudgetFragment extends Fragment {

    private TextView tvCurrentMonth;
    private Calendar calendar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        tvCurrentMonth = view.findViewById(R.id.tvCurrentMonth);
        ImageButton btnPrev = view.findViewById(R.id.btnPrevMonth);
        ImageButton btnNext = view.findViewById(R.id.btnNextMonth);

        calendar = Calendar.getInstance();
        updateMonthText();

        btnPrev.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            updateMonthText();
        });

        btnNext.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            updateMonthText();
        });

        return view;
    }

    private void updateMonthText() {
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        tvCurrentMonth.setText("Tháng " + month + " / " + year);
    }
}
