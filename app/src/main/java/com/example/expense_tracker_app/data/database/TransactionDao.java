package com.example.expense_tracker_app.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.expense_tracker_app.data.model.Category;
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

    @Delete
    void deleteTransaction(Transaction transaction);

    @Insert
    long insertCategory(Category category);

    @Insert
    long insertSubcategory(Subcategory subcategory);

    // --- SỬA: Dùng IN (:types) để lấy được cả INCOME và DEBT_COLLECTION ---
    @androidx.room.Transaction
    @Query("SELECT * FROM categories WHERE type IN (:types) AND userId = :userId")
    List<CategoryWithSubcategories> getCategoriesWithSubcategories(List<TxType> types, int userId);
    // ---------------------------------------------------------------------

    @Query("SELECT * FROM categories WHERE userId = :userId")
    List<Category> getAllCategories(int userId);

    @Query("SELECT * FROM subcategories WHERE categoryId = :categoryId AND userId = :userId")
    List<Subcategory> getSubcategoriesByCategory(int categoryId, int userId);

    @Query("SELECT * FROM subcategories WHERE id = :id LIMIT 1")
    Subcategory findSubcategoryById(int id);

    @Query("SELECT COUNT(*) FROM categories WHERE userId = :userId")
    int countCategoriesByUserId(int userId);

    @Query("SELECT COUNT(*) FROM subcategories WHERE userId = :userId")
    int countSubcategoriesByUserId(int userId);

    @Query("SELECT COUNT(*) FROM categories")
    int countCategories();

    @Query("SELECT COUNT(*) FROM subcategories")
    int countSubcategories();

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC, id DESC")
    LiveData<List<Transaction>> getAllTransactions(int userId);

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date DESC, id DESC")
    LiveData<List<Transaction>> getTransactionsByDateRange(int userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT * FROM transactions WHERE userId = :userId AND subcategoryId IN (:subcategoryIds) AND type = 'EXPENSE' AND date >= :startEpochDay AND date <= :endEpochDay AND excludeFromReport = 0 ORDER BY date DESC, id DESC")
    List<Transaction> getTransactionsBySubcategories(int userId, long startEpochDay, long endEpochDay, List<Integer> subcategoryIds);
}