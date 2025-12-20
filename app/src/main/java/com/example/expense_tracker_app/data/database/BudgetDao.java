package com.example.expense_tracker_app.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.expense_tracker_app.data.model.Budget;
import java.util.List;

@Dao
public interface BudgetDao {
    @Insert
    long insertBudget(Budget budget);

    @Update
    int updateBudget(Budget budget);

    @Query("SELECT * FROM budgets ORDER BY created_at DESC")
    List<Budget> getAllBudgets();

    @Query("SELECT * FROM budgets WHERE id = :id LIMIT 1")
    Budget getBudgetById(int id);

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year ORDER BY created_at DESC")
    List<Budget> getAllBudgetsByMonthYear(int month, int year);

    @Query("SELECT DISTINCT year FROM budgets ORDER BY year DESC")
    List<Integer> getAvailableYears();

    @Query("SELECT DISTINCT month FROM budgets WHERE year = :year ORDER BY month ASC")
    List<Integer> getAvailableMonthsForYear(int year);
}
