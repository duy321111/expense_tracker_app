
package com.example.expense_tracker_app.ui.stats;
import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.TxType; 
import com.example.expense_tracker_app.ui.Month.MonthAdapter;
import com.example.expense_tracker_app.ui.Month.MonthItem;
import com.example.expense_tracker_app.ui.TransactionListActivity; 
import com.google.android.material.card.MaterialCardView; 

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import androidx.lifecycle.Observer;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.repository.TransactionRepository;
import android.app.Application;


public class StatsActivity extends Fragment {

    private ImageView ivBack, ivCalendar;
    private Spinner spnMonthFrom, spnMonthTo;
    private LinearLayout chartContainer;
    private TextView tvIncomeTotal, tvExpenseTotal;

    private MaterialCardView cardIncome, cardExpense;

    private LinearLayout yAxis;
    private FrameLayout gridLayer;

    private MonthAdapter monthAdapter;
    private final List<MonthItem> monthItems = new ArrayList<>();
    private TransactionRepository transactionRepository;

    private int userId = 1; // TODO: lấy userId thực tế nếu có session
    private int selectedMonth, selectedYear;
    private final List<Object> selectedPeriods = new ArrayList<>(); // Dummy, to avoid errors
    private List<Transaction> transactionsList = new ArrayList<>();


@Override
public View onCreateView(@NonNull LayoutInflater inflater,
                         @Nullable ViewGroup container,
                         @Nullable Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.activity_stats, container, false);

    ivBack = root.findViewById(R.id.ivBack);
    ivCalendar = root.findViewById(R.id.ivCalendar);
    spnMonthFrom = root.findViewById(R.id.spnMonthFrom);
    spnMonthTo = root.findViewById(R.id.spnMonthTo);
    chartContainer = root.findViewById(R.id.chartContainer);
    tvIncomeTotal = root.findViewById(R.id.tvIncomeTotal);
    tvExpenseTotal = root.findViewById(R.id.tvExpenseTotal);

    yAxis = root.findViewById(R.id.yAxis);
    gridLayer = root.findViewById(R.id.gridLayer);

    cardIncome = root.findViewById(R.id.cardIncome);
    cardExpense = root.findViewById(R.id.cardExpense);


    transactionRepository = new TransactionRepository((Application) requireActivity().getApplication());
    // Lấy min/max tháng từ giao dịch thực tế, chỉ observe 1 lần
    transactionRepository.getAllTransactions(userId).observe(getViewLifecycleOwner(), new Observer<List<Transaction>>() {
        @Override
        public void onChanged(List<Transaction> transactions) {
            transactionsList = (transactions == null) ? new ArrayList<>() : transactions;
            if (transactionsList.isEmpty()) {
                initCalendarDefaults();
                setupMonthSpinnersWithRange(LocalDate.now(), LocalDate.now());
            } else {
                LocalDate minDate = transactionsList.get(0).date;
                LocalDate maxDate = transactionsList.get(0).date;
                for (Transaction t : transactionsList) {
                    if (t.date.isBefore(minDate)) minDate = t.date;
                    if (t.date.isAfter(maxDate)) maxDate = t.date;
                }
                initCalendarDefaults();
                setupMonthSpinnersWithRange(minDate.withDayOfMonth(1), maxDate.withDayOfMonth(1));
            }
            setupClicks();
        }
    });

    return root;
}

private void initCalendarDefaults() {
    LocalDate now = LocalDate.now();
    selectedYear = now.getYear();
    selectedMonth = now.getMonthValue();
}

private void setupMonthSpinnersWithRange(LocalDate minMonth, LocalDate maxMonth) {
    monthItems.clear();
    LocalDate iter = minMonth.withDayOfMonth(1);
    LocalDate end = maxMonth.withDayOfMonth(1);
    while (!iter.isAfter(end)) {
        monthItems.add(new MonthItem(iter.getYear(), iter.getMonthValue()));
        iter = iter.plusMonths(1);
    }

    List<String> monthLabels = new ArrayList<>();
    for (MonthItem item : monthItems) {
        monthLabels.add(String.format(Locale.getDefault(), "%02d/%d", item.month, item.year));
    }

    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, monthLabels);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spnMonthFrom.setAdapter(adapter);
    spnMonthTo.setAdapter(adapter);

    // Default: chọn tháng cuối cùng (max) cho cả 2
    int currentIndex = monthItems.size() - 1;
    spnMonthFrom.setSelection(currentIndex);
    spnMonthTo.setSelection(currentIndex);

    AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            int fromIdx = spnMonthFrom.getSelectedItemPosition();
            int toIdx = spnMonthTo.getSelectedItemPosition();
            if (fromIdx > toIdx) {
                // Prevent invalid range: auto-correct
                if (parent == spnMonthFrom) {
                    spnMonthTo.setSelection(fromIdx);
                } else {
                    spnMonthFrom.setSelection(toIdx);
                }
                return;
            }
            selectedPeriods.clear();
            for (int i = fromIdx; i <= toIdx; i++) {
                MonthItem m = monthItems.get(i);
                selectedPeriods.add(m); // No need CalendarSheet.Period
            }
            renderChartAndSummary();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };
    spnMonthFrom.setOnItemSelectedListener(listener);
    spnMonthTo.setOnItemSelectedListener(listener);
}

private void setupClicks() {
    ivBack.setOnClickListener(v -> requireActivity().onBackPressed());
    ivCalendar.setVisibility(View.GONE); // Ẩn luôn nút calendar
    cardIncome.setOnClickListener(v -> {
        int fromIdx = spnMonthFrom.getSelectedItemPosition();
        int toIdx = spnMonthTo.getSelectedItemPosition();
        MonthItem from = monthItems.get(fromIdx);
        MonthItem to = monthItems.get(toIdx);
        Intent intent = new Intent(requireContext(), TransactionListActivity.class);
        intent.putExtra("tx_type", TxType.INCOME.name());
        intent.putExtra("month", selectedMonth);
        intent.putExtra("year", selectedYear);
        intent.putExtra("from_month", from.month);
        intent.putExtra("from_year", from.year);
        intent.putExtra("to_month", to.month);
        intent.putExtra("to_year", to.year);
        startActivity(intent);
    });
    cardExpense.setOnClickListener(v -> {
        int fromIdx = spnMonthFrom.getSelectedItemPosition();
        int toIdx = spnMonthTo.getSelectedItemPosition();
        MonthItem from = monthItems.get(fromIdx);
        MonthItem to = monthItems.get(toIdx);
        Intent intent = new Intent(requireContext(), TransactionListActivity.class);
        intent.putExtra("tx_type", TxType.EXPENSE.name());
        intent.putExtra("month", selectedMonth);
        intent.putExtra("year", selectedYear);
        intent.putExtra("from_month", from.month);
        intent.putExtra("from_year", from.year);
        intent.putExtra("to_month", to.month);
        intent.putExtra("to_year", to.year);
        startActivity(intent);
    });
}

private int findIndexNow(List<MonthItem> list) {
    LocalDate now = LocalDate.now();
    for (int i = 0; i < list.size(); i++) {
        MonthItem it = list.get(i);
        if (it.year == now.getYear() && it.month == now.getMonthValue()) return i;
    }
    return list.size() / 2;
}

/** ===== Chart ===== */
private void renderChartAndSummary() {
    chartContainer.removeAllViews();
    if (yAxis != null) yAxis.removeAllViews();
    if (gridLayer != null) gridLayer.removeAllViews();

    int fromIdx = spnMonthFrom.getSelectedItemPosition();
    int toIdx = spnMonthTo.getSelectedItemPosition();
    if (fromIdx > toIdx) return;
    if (monthItems.isEmpty()) return;
    LocalDate fromMonth = LocalDate.of(monthItems.get(fromIdx).year, monthItems.get(fromIdx).month, 1);
    LocalDate toMonth = LocalDate.of(monthItems.get(toIdx).year, monthItems.get(toIdx).month, 1);
    LocalDate startDate = fromMonth.withDayOfMonth(1);
    LocalDate endDate = toMonth.withDayOfMonth(toMonth.lengthOfMonth());

    long incomeSum = 0, expenseSum = 0;
    List<Transaction> filtered = new ArrayList<>();
    for (Transaction t : transactionsList) {
        if ((t.date.isEqual(startDate) || t.date.isAfter(startDate)) && (t.date.isEqual(endDate) || t.date.isBefore(endDate))) {
            filtered.add(t);
            if (t.type == TxType.INCOME || t.type == TxType.DEBT_COLLECTION) incomeSum += t.amount;
            if (t.type == TxType.EXPENSE || t.type == TxType.LOAN_REPAYMENT) expenseSum += t.amount;
        }
    }

    long rawMax = 0;
    List<LocalDate> months = new ArrayList<>();
    String fromStr = String.format("%04d-%02d", fromMonth.getYear(), fromMonth.getMonthValue());
    String toStr = String.format("%04d-%02d", toMonth.getYear(), toMonth.getMonthValue());
    if (fromStr.equals(toStr)) {
        months.add(fromMonth);
    } else {
        LocalDate iter = fromMonth;
        while (!iter.isAfter(toMonth)) {
            months.add(iter);
            iter = iter.plusMonths(1);
        }
    }
    List<Long> incomeByMonth = new ArrayList<>();
    List<Long> expenseByMonth = new ArrayList<>();
    Log.d("StatsActivity", "months.size = " + months.size());
    if (months.size() == 1) {
        LocalDate m = months.get(0);
        long income = 0, expense = 0;
        for (Transaction t : filtered) {
            if (t.date.getYear() == m.getYear() && t.date.getMonthValue() == m.getMonthValue()) {
                if (t.type == TxType.INCOME || t.type == TxType.DEBT_COLLECTION) income += t.amount;
                if (t.type == TxType.EXPENSE || t.type == TxType.LOAN_REPAYMENT) expense += t.amount;
            }
        }
        incomeByMonth.add(income);
        expenseByMonth.add(expense);
        rawMax = Math.max(rawMax, Math.max(income, expense));
    } else {
        for (LocalDate m : months) {
            long income = 0, expense = 0;
            for (Transaction t : filtered) {
                if (t.date.getYear() == m.getYear() && t.date.getMonthValue() == m.getMonthValue()) {
                    if (t.type == TxType.INCOME || t.type == TxType.DEBT_COLLECTION) income += t.amount;
                    if (t.type == TxType.EXPENSE || t.type == TxType.LOAN_REPAYMENT) expense += t.amount;
                }
            }
            incomeByMonth.add(income);
            expenseByMonth.add(expense);
            rawMax = Math.max(rawMax, Math.max(income, expense));
        }
    }
    final long niceMax = niceTopByHalfSteps(rawMax);
    final int ticks = 4;
    final float plotHeightPx = dp(160);

    for (int i = 0; i < months.size(); i++) {
        LocalDate m = months.get(i);
        long income = incomeByMonth.get(i);
        long expense = expenseByMonth.get(i);
        if (income == 0 && expense == 0) continue;

        View group = getLayoutInflater().inflate(R.layout.view_bar_group, chartContainer, false);
        TextView tvLabel = group.findViewById(R.id.tvLabel);
        View incomeBar = group.findViewById(R.id.vIncome);
        View expenseBar = group.findViewById(R.id.vExpense);

        tvLabel.setText(String.format(Locale.getDefault(), "%02d/%d", m.getMonthValue(), m.getYear()));

        int hIncome = (income == 0) ? 0 : Math.max(dp(8), Math.round(plotHeightPx * (income / (float) niceMax)));
        int hExpense = (expense == 0) ? 0 : Math.max(dp(8), Math.round(plotHeightPx * (expense / (float) niceMax)));
        incomeBar.getLayoutParams().height = hIncome;
        expenseBar.getLayoutParams().height = hExpense;
        incomeBar.requestLayout();
        expenseBar.requestLayout();

        group.post(() -> {
            int w = incomeBar.getWidth();
            expenseBar.setTranslationX(-w / 3f);
            expenseBar.bringToFront();
        });

        chartContainer.addView(group);
    }

    chartContainer.post(() -> {
        if (chartContainer.getChildCount() == 0) return;
        View firstGroup = chartContainer.getChildAt(0);
        View barRow = firstGroup.findViewById(R.id.barRow);
        int plotTop = barRow.getTop();

        if (yAxis != null) yAxis.removeAllViews();
        if (gridLayer != null) gridLayer.removeAllViews();

        for (int i = ticks; i >= 0; i--) {
            float y = plotHeightPx - (plotHeightPx * i / (float) ticks);

            View line = new View(requireContext());
            line.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.neutral_200));
            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, dp(1));
            flp.topMargin = plotTop + Math.round(y);
            gridLayer.addView(line, flp);

            TextView t = new TextView(requireContext());
            t.setText(formatShort(niceMax * i / ticks));
            t.setTextSize(11);
            t.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            yAxis.addView(t, lp);

            final int idx = i;
            t.post(() -> {
                float targetY = plotTop + (plotHeightPx - (plotHeightPx * idx / (float) ticks));
                float centered = targetY - t.getHeight() / 2f;
                t.setTranslationY(centered - t.getTop());
            });
        }
    });


    tvIncomeTotal.setText(formatThousand(incomeSum) + " đ");
    tvExpenseTotal.setText(formatThousand(expenseSum) + " đ");
}

private int dp(int v){ return Math.round(getResources().getDisplayMetrics().density * v); }

private long niceTopByHalfSteps(long v){
    if (v <= 0) return 1;
    double step = v / 4.0;
    double base = Math.pow(10, Math.floor(Math.log10(step)));
    double n = step / base;
    while (n > 5) { base *= 10; n /= 10.0; }
    double[] mult = {0.5, 1, 1.5, 2, 2.5, 3, 4, 5};
    double chosen = 5;
    for (double m : mult) { if (n <= m) { chosen = m; break; } }
    double top = Math.ceil(chosen * base * 4.0);
    return Math.max((long)top, 1);
}

private String formatShort(long value){
    if (value >= 1_000_000L) {
        if (value % 1_000_000L == 0) return (value / 1_000_000L) + "m";
        return trimTrailingZero(String.format(Locale.US, "%.1fm", value / 1_000_000f));
    } else if (value >= 100_000L) {
        return (value / 1000L) + "k";
    } else if (value >= 1000L) {
        if (value % 1000L == 0) return (value / 1000L) + "k";
        return trimTrailingZero(String.format(Locale.US, "%.1fk", value / 1000f));
    }
    return String.valueOf(value);
}

private String formatThousand(long v){
    return NumberFormat.getInstance(new Locale("vi","VN")).format(v);
}

private String trimTrailingZero(String s){
    return s.replaceAll("\\.0([a-z])$", "$1");
}

private LocalDate startOf(CalendarSheet.Period p){
    try{
        switch (p.type){
            case DAY: return LocalDate.parse(p.key.substring(2));
            case WEEK: return LocalDate.parse(p.key.substring(2).split("_")[0]);
            case MONTH: {
                String[] a = p.key.substring(2).split("-");
                int y = Integer.parseInt(a[0]);
                int m = Integer.parseInt(a[1]);
                return LocalDate.of(y, m, 1);
            }
            case QUARTER: {
                String[] a = p.key.substring(2).split("-");
                int y = Integer.parseInt(a[0]);
                int q = Integer.parseInt(a[1]);
                int m = (q - 1) * 3 + 1;
                return LocalDate.of(y, m, 1);
            }
            case YEAR:
            default: {
                int y = Integer.parseInt(p.key.substring(2));
                return LocalDate.of(y, 1, 1);
            }
        }
    } catch (Exception e){ return LocalDate.of(1970,1,1); }
}

private List<CalendarSheet.Period> sortedPeriodsAsc(List<CalendarSheet.Period> in){
    ArrayList<CalendarSheet.Period> out = new ArrayList<>(in);
    out.sort((a,b) -> startOf(a).compareTo(startOf(b)));
    return out;
}


}