package com.example.expense_tracker_app.data.model;

public class TransactionItem {
    private final String categoryName;
    private final String paymentMethod;
    private final String amount;
    private final int iconResId; // ID của icon (ví dụ: R.drawable.ic_food)
    private final boolean isExpense; // True nếu là chi tiêu, False nếu là thu nhập

    public TransactionItem(String categoryName, String paymentMethod, String amount, int iconResId, boolean isExpense) {
        this.categoryName = categoryName;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.iconResId = iconResId;
        this.isExpense = isExpense;
    }

    // Getters
    public String getCategoryName() { return categoryName; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getAmount() { return amount; }
    public int getIconResId() { return iconResId; }
    public boolean isExpense() { return isExpense; }
}