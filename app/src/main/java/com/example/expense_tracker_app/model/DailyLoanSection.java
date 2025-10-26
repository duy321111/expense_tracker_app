package com.example.expense_tracker_app.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DailyLoanSection {
    private Date date;
    private List<LoanTransaction> transactions;

    public DailyLoanSection(Date date) {
        this.date = date;
        this.transactions = new ArrayList<>();
    }

    public Date getDate() { return date; }
    public List<LoanTransaction> getTransactions() { return transactions; }

    public void setDate(Date date) { this.date = date; }
    public void setTransactions(List<LoanTransaction> transactions) {
        this.transactions = transactions;
    }

    public void addTransaction(LoanTransaction transaction) {
        this.transactions.add(transaction);
    }
}