package com.example.expense_tracker_app.data.model;

import java.util.Date;

public class LoanTransaction {
    private String id;
    private String type; // "borrow" (đi vay) or "lend" (cho vay)
    private String personName;
    private double amount;
    private Date date;
    private String status;
    private int iconColor;

    public LoanTransaction(String id, String type, String personName,
                           double amount, Date date, String status, int iconColor) {
        this.id = id;
        this.type = type;
        this.personName = personName;
        this.amount = amount;
        this.date = date;
        this.status = status;
        this.iconColor = iconColor;
    }

    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public String getPersonName() { return personName; }
    public double getAmount() { return amount; }
    public Date getDate() { return date; }
    public String getStatus() { return status; }
    public int getIconColor() { return iconColor; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setPersonName(String personName) { this.personName = personName; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDate(Date date) { this.date = date; }
    public void setStatus(String status) { this.status = status; }
    public void setIconColor(int iconColor) { this.iconColor = iconColor; }

    public String getTypeDisplay() {
        return type.equals("borrow") ? "Đi vay" : "Cho vay";
    }
}
