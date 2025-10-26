package com.example.expense_tracker_app.model;

public class ExpenseDetail {
    private String title;
    private double amount;
    private String frequency; // /ngày, /tuần, /tháng
    private int iconRes;

    public ExpenseDetail(String title, double amount, String frequency, int iconRes) {
        this.title = title;
        this.amount = amount;
        this.frequency = frequency;
        this.iconRes = iconRes;
    }

    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public String getFrequency() { return frequency; }
    public int getIconRes() { return iconRes; }

    public void setTitle(String title) { this.title = title; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public void setIconRes(int iconRes) { this.iconRes = iconRes; }
}