package com.example.expense_tracker_app.data.model;

public class Category {
    public final int id;
    public final String name;
    public final TxType type;
    public final String icon; // Icon resource name (e.g., "ic_cat_food")

    public Category(int id, String name, TxType type, String icon) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.icon = icon;
    }

    // Giữ constructor đơn giản cho các trường hợp mock/default
    public Category(String n) {
        this.id = 0;
        this.name = n;
        this.type = TxType.EXPENSE;
        this.icon = "";
    }

    @Override public String toString(){ return name; }
}