package com.example.expense_tracker_app.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.expense_tracker_app.data.model.*;
import com.example.expense_tracker_app.data.repository.InMemoryRepo;
import com.example.expense_tracker_app.data.repository.Repository;

import java.time.LocalDate;
import java.util.*;

public class AddTxViewModel extends ViewModel {
    private final Repository repo = new InMemoryRepo();

    public final MutableLiveData<TxType> type = new MutableLiveData<>(TxType.EXPENSE);
    public final MutableLiveData<Category> category = new MutableLiveData<>();
    public final MutableLiveData<String> method = new MutableLiveData<>("Tiền mặt");
    public final MutableLiveData<String> amount = new MutableLiveData<>("");
    public final MutableLiveData<String> note = new MutableLiveData<>("");
    public final MutableLiveData<LocalDate> date = new MutableLiveData<>(LocalDate.now());
    public final MutableLiveData<Boolean> done = new MutableLiveData<>(false);

    private final TxType[] TX_TYPES = {TxType.EXPENSE, TxType.INCOME, TxType.BORROW, TxType.LEND, TxType.ADJUST};

    public boolean isSelectableCategoryType(){
        return type.getValue() == TxType.INCOME || type.getValue() == TxType.EXPENSE;
    }

    public void setTypeByIndex(int index) {
        if (index >= 0 && index < TX_TYPES.length) {
            type.setValue(TX_TYPES[index]);
            category.setValue(null);
        }
    }

    public List<Category> categories(){
        return repo.categoriesBy(type.getValue());
    }

    public void submit(){
        long amountVal = 0;
        try {
            amountVal = Long.parseLong(amount.getValue());
        } catch (NumberFormatException e) {
            // Nên có validation/thông báo lỗi
            return;
        }

        if (category.getValue() == null) {
            // Nếu không chọn danh mục (ví dụ khi nhấn Bỏ qua), lấy mặc định
            if (isSelectableCategoryType()) {
                // Nên có thông báo lỗi chưa chọn danh mục
                return;
            }
        }

        Category finalCat = category.getValue();
        if (finalCat == null) {
            // Cho các loại không cần chọn category (Borrow/Lend/Adjust), gán mặc định từ type
            finalCat = new Category(type.getValue().name());
        }


        Transaction newTx = new Transaction(
                new Random().nextInt(10000),
                type.getValue(),
                finalCat,
                amountVal,
                method.getValue(),
                date.getValue(),
                note.getValue()
        );

        repo.addTransaction(newTx);
        done.setValue(true);
    }
}