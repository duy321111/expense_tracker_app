package com.example.expense_tracker_app;

public class Expense {
    private String categoryName;
    private double amount;
    private String date;

    public Expense(String categoryName, double amount, String date) {
        this.categoryName = categoryName;
        this.amount = amount;
        this.date = date;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }
}