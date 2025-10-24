package com.example.expense_tracker_app.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.expense_tracker_app.data.model.*;
import java.time.LocalDate;
import java.util.*;

public class AddTxViewModel extends ViewModel {
    public final MutableLiveData<TxType> type = new MutableLiveData<>(TxType.EXPENSE);
    public final MutableLiveData<Category> category = new MutableLiveData<>();
    public final MutableLiveData<String> method = new MutableLiveData<>("Tiền mặt");
    public final MutableLiveData<String> amount = new MutableLiveData<>("");
    public final MutableLiveData<String> note = new MutableLiveData<>("");
    public final MutableLiveData<LocalDate> date = new MutableLiveData<>(LocalDate.now());
    public final MutableLiveData<Boolean> done = new MutableLiveData<>(false);

    public List<Category> categories(){
        if(type.getValue()== TxType.INCOME)
            return Arrays.asList(new Category("Bán hàng"), new Category("Tiền lương"), new Category("Được trả nợ"));
        else
            return Arrays.asList(new Category("Ăn uống"), new Category("Cà phê"), new Category("Đi chợ"));
    }

    public void submit(){
        done.setValue(true); // demo
    }
}
