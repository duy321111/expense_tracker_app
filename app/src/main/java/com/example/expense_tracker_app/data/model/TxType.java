package com.example.expense_tracker_app.data.model;

public enum TxType {
    INCOME,
    EXPENSE,
    BORROW,
    LEND,
    ADJUST,
    // --- THÊM 2 LOẠI MỚI ---
    DEBT_COLLECTION, // Thu hồi nợ (Tiền vào ví -> Giống Income)
    LOAN_REPAYMENT   // Trả nợ (Tiền ra ví -> Giống Expense)
}