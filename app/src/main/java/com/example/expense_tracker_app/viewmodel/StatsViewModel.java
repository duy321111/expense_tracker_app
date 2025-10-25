package com.example.expense_tracker_app.viewmodel;

import androidx.lifecycle.*;
import com.example.expense_tracker_app.data.model.MonthlyStat;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.repository.InMemoryRepo;
import com.example.expense_tracker_app.data.repository.Repository;
import com.example.expense_tracker_app.ui.BarChartView;
import com.example.expense_tracker_app.utils.CurrencyUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class StatsViewModel extends ViewModel {
    private final Repository repo = new InMemoryRepo();
    private final MutableLiveData<Integer> year = new MutableLiveData<>(LocalDate.now().getYear());
    private final MutableLiveData<Integer> month = new MutableLiveData<>(-1);
    private final MutableLiveData<List<Transaction>> txs = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<BarChartView.Bar>> chart = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> income = new MutableLiveData<>("0 đ");
    private final MutableLiveData<String> expense = new MutableLiveData<>("0 đ");

    public LiveData<List<Transaction>> txs(){ return txs; }
    public LiveData<List<BarChartView.Bar>> chart(){ return chart; }
    public LiveData<String> income(){ return income; }
    public LiveData<String> expense(){ return expense; }
    public LiveData<Integer> month(){ return month; }

    public void setMonth(int m){ month.setValue(m); recalc(); }

    public StatsViewModel(){
        setMonth(LocalDate.now().getMonthValue());
    }

    private void recalc(){
        int currentYear = year.getValue();
        int currentMonth = month.getValue();
        if (currentMonth == -1) return;

        List<Transaction> transactions = repo.transactionsByMonth(currentYear, currentMonth);
        List<MonthlyStat> stats = repo.dailyStats(currentYear, currentMonth);

        long totalIncome = 0;
        long totalExpense = 0;

        for(MonthlyStat stat : stats) {
            totalIncome += stat.income;
            totalExpense += stat.expense;
        }

        income.setValue(CurrencyUtils.vnd(totalIncome));
        expense.setValue(CurrencyUtils.vnd(totalExpense));
        txs.setValue(transactions);

        // Chuẩn bị dữ liệu cho biểu đồ (lấy 4 ngày có tổng giao dịch lớn nhất)
        List<BarChartView.Bar> bars = stats.stream()
                .filter(s -> s.income > 0 || s.expense > 0)
                .sorted((a, b) -> Long.compare(b.income + b.expense, a.income + a.expense))
                .limit(4)
                .map(s -> new BarChartView.Bar(
                        String.valueOf(s.day),
                        (float)s.income / 1_000_000f, // Giảm tỷ lệ để dễ vẽ biểu đồ
                        (float)s.expense / 1_000_000f
                ))
                .sorted((a, b) -> Integer.compare(Integer.parseInt(a.label), Integer.parseInt(b.label))) // Sắp xếp lại theo ngày
                .collect(Collectors.toList());

        chart.setValue(bars);
    }

    public int getMonth(){ return month.getValue()==null?-1:month.getValue(); }
    public int getYear(){ return year.getValue(); }
}