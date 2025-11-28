package com.example.expense_tracker_app.data.repository;

import android.content.Context;
import com.example.expense_tracker_app.data.database.AppDatabase;
import com.example.expense_tracker_app.data.database.BudgetDao;
import com.example.expense_tracker_app.data.model.Budget;
import java.util.List;

public class BudgetRepository {
    private BudgetDao budgetDao;

    public BudgetRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        budgetDao = db.budgetDao();
    }
    public boolean addBudget(Budget budget) {
        try {
            long id = budgetDao.insertBudget(budget);
            return id > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Budget> getAllBudgets() {
        return budgetDao.getAllBudgets();
    }
    public Budget getBudgetById(int id) {
        return budgetDao.getBudgetById(id);
    }
    public List<Budget> getBudgetsByMonthYear(int month, int year) {
        return budgetDao.getAllBudgetsByMonthYear(month, year);
    }

    public List<Integer> getAvailableYears() {
        return budgetDao.getAvailableYears();
    }

    public List<Integer> getAvailableMonthsForYear(int year) {
        return budgetDao.getAvailableMonthsForYear(year);
    }

    public boolean updateBudget(Budget budget) {
        try {
            int rowsAffected = budgetDao.updateBudget(budget);
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
