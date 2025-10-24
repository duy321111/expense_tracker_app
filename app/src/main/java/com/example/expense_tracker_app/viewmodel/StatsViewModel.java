package com.example.expense_tracker_app.viewmodel;

import androidx.lifecycle.*;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.TxType;
import com.example.expense_tracker_app.ui.BarChartView;
import java.util.*;

public class StatsViewModel extends ViewModel {
    private final MutableLiveData<Integer> month = new MutableLiveData<>(3);
    private final MutableLiveData<List<Transaction>> txs = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<BarChartView.Bar>> chart = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Long> income = new MutableLiveData<>(0L);
    private final MutableLiveData<Long> expense = new MutableLiveData<>(0L);

    public LiveData<List<Transaction>> txs(){ return txs; }
    public LiveData<List<BarChartView.Bar>> chart(){ return chart; }
    public LiveData<Long> income(){ return income; }
    public LiveData<Long> expense(){ return expense; }
    public void setMonth(int m){ month.setValue(m); recalc(); }

    public StatsViewModel(){ seed(); recalc(); }

    private void seed(){
        List<Transaction> list = new ArrayList<>();
        list.add(Transaction.fake("Bán hàng",  400000, TxType.INCOME,"Tiền mặt"));
        list.add(Transaction.fake("Tiền lương",3000000, TxType.INCOME,"Chuyển khoản"));
        list.add(Transaction.fake("Ăn uống",   99000,   TxType.EXPENSE,"Tiền mặt"));
        list.add(Transaction.fake("Cà phê",    150000,  TxType.EXPENSE,"Tiền mặt"));
        list.add(Transaction.fake("Trả nợ",    2500000, TxType.INCOME,"Chuyển khoản"));
        txs.setValue(list);
    }

    private void recalc(){
        long inc=0, exp=0;
        for(Transaction t: txs.getValue()){
            if(t.type== TxType.INCOME) inc += t.amount; else exp += t.amount;
        }
        income.setValue(inc); expense.setValue(exp);

        List<BarChartView.Bar> bars = new ArrayList<>();
        bars.add(new BarChartView.Bar("01", 1.2f, 0.6f));
        bars.add(new BarChartView.Bar("10", 2.0f, 1.2f));
        bars.add(new BarChartView.Bar("20", 2.4f, 0.9f));
        bars.add(new BarChartView.Bar("30", 1.6f, 1.8f));
        chart.setValue(bars);
    }

    public int getMonth(){ return month.getValue()==null?1:month.getValue(); }
    public int getYear(){ return 2023; }
}
