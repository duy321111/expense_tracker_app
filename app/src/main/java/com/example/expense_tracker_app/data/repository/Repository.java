package com.example.expense_tracker_app.data.repository;

import com.example.expense_tracker_app.data.model.Category;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.TxType;
import com.example.expense_tracker_app.data.model.MonthlyStat;

import java.util.List;

public interface Repository {
    // Lấy danh mục theo loại giao dịch
    List<Category> categoriesBy(TxType type);

    // Thêm danh mục (mặc định EXPENSE)
    void addCategory(Category c);

    // Thêm danh mục với loại cụ thể
    void addCategory(Category c, TxType type);

    // Thêm giao dịch
    void addTransaction(Transaction t);

    // Lấy danh sách giao dịch theo tháng
    List<Transaction> transactionsByMonth(int year, int month);

    // ✅ Thêm thống kê theo ngày trong tháng (cho StatsViewModel)
    List<MonthlyStat> dailyStats(int year, int month);
}
