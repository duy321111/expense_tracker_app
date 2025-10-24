package com.example.expense_tracker_app.data.repository;

import com.example.expense_tracker_app.data.model.*;
import java.util.List;

public interface Repository {
    List<Category> categoriesBy(TxType type);
    void addTransaction(Transaction tx);
    List<Transaction> transactionsByMonth(int year, int month);
    List<MonthlyStat> dailyStats(int year, int month);
}
