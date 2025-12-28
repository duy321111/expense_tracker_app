package com.example.expense_tracker_app.ui.stats;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

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

public class StatsActivity extends Fragment implements CalendarSheet.OnApplySelectionListener {

    private ImageView ivBack, ivCalendar;
    private RecyclerView rvMonths;
    private LinearLayout chartContainer;
    private TextView tvIncomeTotal, tvExpenseTotal;

    private MaterialCardView cardIncome, cardExpense;

    private LinearLayout yAxis;
    private FrameLayout gridLayer;

    private MonthAdapter monthAdapter;
    private final List<MonthItem> monthItems = new ArrayList<>();
    private int selectedMonth, selectedYear;
    private final List<CalendarSheet.Period> selectedPeriods = new ArrayList<>();


@Override
public View onCreateView(@NonNull LayoutInflater inflater,
                         @Nullable ViewGroup container,
                         @Nullable Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.activity_stats, container, false);

    ivBack = root.findViewById(R.id.ivBack);
    ivCalendar = root.findViewById(R.id.ivCalendar);
    rvMonths = root.findViewById(R.id.rvMonths);
    chartContainer = root.findViewById(R.id.chartContainer);
    tvIncomeTotal = root.findViewById(R.id.tvIncomeTotal);
    tvExpenseTotal = root.findViewById(R.id.tvExpenseTotal);

    yAxis = root.findViewById(R.id.yAxis);
    gridLayer = root.findViewById(R.id.gridLayer);

    cardIncome = root.findViewById(R.id.cardIncome);
    cardExpense = root.findViewById(R.id.cardExpense);

    initCalendarDefaults();
    setupMonthStrip();
    setupClicks();

    selectedPeriods.clear();
    selectedPeriods.add(CalendarSheet.Period.forMonth(selectedYear, selectedMonth));
    renderChartAndSummary();


    return root;
}

private void initCalendarDefaults() {
    LocalDate now = LocalDate.now();
    selectedYear = now.getYear();
    selectedMonth = now.getMonthValue();
}

private void setupMonthStrip() {
    monthItems.clear();
    LocalDate now = LocalDate.now().minusMonths(24);
    for (int i = 0; i <= 48; i++) {
        LocalDate d = now.plusMonths(i);
        monthItems.add(new MonthItem(d.getYear(), d.getMonthValue()));
    }

    LinearLayoutManager lm = new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false);
    rvMonths.setLayoutManager(lm);
    monthAdapter = new MonthAdapter(monthItems);
    rvMonths.setAdapter(monthAdapter);

    PagerSnapHelper snap = new PagerSnapHelper();
    snap.attachToRecyclerView(rvMonths);

    int currentIndex = findIndexNow(monthItems);
    rvMonths.post(() -> {
        monthAdapter.selected = currentIndex;
        rvMonths.scrollToPosition(currentIndex);
        monthAdapter.notifyDataSetChanged();

        MonthItem m = monthItems.get(currentIndex);
        selectedYear = m.year;
        selectedMonth = m.month;

        selectedPeriods.clear();
        selectedPeriods.add(CalendarSheet.Period.forMonth(selectedYear, selectedMonth));
        renderChartAndSummary();
    });

    rvMonths.addOnScrollListener(new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView r, int state) {
            if (state != RecyclerView.SCROLL_STATE_IDLE) return;
            View v = snap.findSnapView(lm);
            if (v == null) return;
            int idx = lm.getPosition(v);
            if (idx == RecyclerView.NO_POSITION || idx == monthAdapter.selected) return;
            monthAdapter.selected = idx;
            monthAdapter.notifyDataSetChanged();
            MonthItem cur = monthItems.get(idx);
            selectedYear = cur.year;
            selectedMonth = cur.month;

            selectedPeriods.clear();
            selectedPeriods.add(CalendarSheet.Period.forMonth(selectedYear, selectedMonth));
            renderChartAndSummary();
        }
    });
}

private void setupClicks() {
    ivBack.setOnClickListener(v -> requireActivity().onBackPressed());
    ivCalendar.setOnClickListener(v -> {
        CalendarSheet sheet = CalendarSheet.newInstance(selectedPeriods);
        sheet.show(getParentFragmentManager(), "CalendarSheet");
    });
    cardIncome.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TransactionListActivity.class);
            intent.putExtra("tx_type", TxType.INCOME.name());
            intent.putExtra("month", selectedMonth);
            intent.putExtra("year", selectedYear);
            startActivity(intent);
        });
    cardExpense.setOnClickListener(v -> {
        Intent intent = new Intent(requireContext(), TransactionListActivity.class);
        intent.putExtra("tx_type", TxType.EXPENSE.name());
        intent.putExtra("month", selectedMonth);
        intent.putExtra("year", selectedYear);
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

    List<CalendarSheet.Period> periods = sortedPeriodsAsc(selectedPeriods);

    long incomeSum = 0, expenseSum = 0;
    long rawMax = 0;
    for (CalendarSheet.Period p : periods) {
        long income = 800_000 + Math.abs((p.label().hashCode() * 13L) % 1_500_000);
        long expense = 400_000 + Math.abs((p.label().hashCode() * 7L) % 1_200_000);
        rawMax = Math.max(rawMax, Math.max(income, expense));
    }
    final long niceMax = niceTopByHalfSteps(rawMax);
    final int ticks = 4;
    final float plotHeightPx = dp(160);

    for (CalendarSheet.Period p : periods) {
        long income = 800_000 + Math.abs((p.label().hashCode() * 13L) % 1_500_000);
        long expense = 400_000 + Math.abs((p.label().hashCode() * 7L) % 1_200_000);
        incomeSum += income; expenseSum += expense;

        View group = getLayoutInflater().inflate(R.layout.view_bar_group, chartContainer, false);
        TextView tvLabel = group.findViewById(R.id.tvLabel);
        View incomeBar = group.findViewById(R.id.vIncome);
        View expenseBar = group.findViewById(R.id.vExpense);

        tvLabel.setText(p.label());

        int hIncome = Math.max(dp(8), Math.round(plotHeightPx * (income / (float) niceMax)));
        int hExpense = Math.max(dp(8), Math.round(plotHeightPx * (expense / (float) niceMax)));
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

@Override
public void onApply(List<CalendarSheet.Period> periods) {
    if (periods == null || periods.isEmpty()) return;
    CalendarSheet.Mode t = periods.get(0).type;
    ArrayList<CalendarSheet.Period> clean = new ArrayList<>();
    for (CalendarSheet.Period p : periods) if (p.type == t) clean.add(p);

    selectedPeriods.clear();
    selectedPeriods.addAll(clean);
    renderChartAndSummary();
}
}