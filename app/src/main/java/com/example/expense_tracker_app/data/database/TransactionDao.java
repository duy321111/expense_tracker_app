package com.example.expense_tracker_app.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.expense_tracker_app.data.model.Category; // Nhớ Import Category
import com.example.expense_tracker_app.data.model.Transaction;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    void insertTransaction(Transaction transaction);

    // --- PHẦN CATEGORY MỚI ---
    @Insert
    void insertCategory(Category category);

    @Query("SELECT * FROM categories")
    List<Category> getAllCategories();
    // -------------------------

    // Lấy tất cả (Cũ)
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC, id DESC")
    LiveData<List<Transaction>> getAllTransactions(int userId);

    // Lấy theo khoảng thời gian
    @Query("SELECT * FROM transactions WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date DESC, id DESC")
    LiveData<List<Transaction>> getTransactionsByDateRange(int userId, LocalDate startDate, LocalDate endDate);
}