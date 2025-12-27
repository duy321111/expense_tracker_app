package com.example.expense_tracker_app.data.database;    // Lấy giao dịch theo user, tháng/năm, và list subcategoryId


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.expense_tracker_app.data.model.Category; // Nhớ Import Category
import com.example.expense_tracker_app.data.model.CategoryWithSubcategories;
import com.example.expense_tracker_app.data.model.Subcategory;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.TxType;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    long insertTransaction(Transaction transaction);

    // --- PHẦN CATEGORY MỚI ---
    @Insert
    long insertCategory(Category category);

    @Insert
    long insertSubcategory(Subcategory subcategory);

    @androidx.room.Transaction
    @Query("SELECT * FROM categories WHERE type = :type AND userId = :userId")
    List<CategoryWithSubcategories> getCategoriesWithSubcategories(TxType type, int userId);

    @Query("SELECT * FROM categories WHERE userId = :userId")
    List<Category> getAllCategories(int userId);

    @Query("SELECT * FROM subcategories WHERE categoryId = :categoryId AND userId = :userId")
    List<Subcategory> getSubcategoriesByCategory(int categoryId, int userId);

    @Query("SELECT * FROM subcategories WHERE id = :id LIMIT 1")
    Subcategory findSubcategoryById(int id);

    @Query("SELECT COUNT(*) FROM categories")
    int countCategories();

    @Query("SELECT COUNT(*) FROM subcategories")
    int countSubcategories();
    // -------------------------

    // Lấy tất cả (Cũ)
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC, id DESC")
    LiveData<List<Transaction>> getAllTransactions(int userId);

    // Lấy theo khoảng thời gian
    @Query("SELECT * FROM transactions WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date DESC, id DESC")
    LiveData<List<Transaction>> getTransactionsByDateRange(int userId, LocalDate startDate, LocalDate endDate);
    // Lấy giao dịch theo user, khoảng ngày (epoch day), và list subcategoryId
    @Query("SELECT * FROM transactions WHERE userId = :userId AND subcategoryId IN (:subcategoryIds) AND type = 'EXPENSE' AND date >= :startEpochDay AND date <= :endEpochDay ORDER BY date DESC, id DESC")
    List<Transaction> getTransactionsBySubcategories(int userId, long startEpochDay, long endEpochDay, List<Integer> subcategoryIds);

}