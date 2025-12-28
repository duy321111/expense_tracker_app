package com.example.expense_tracker_app.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wallets")
public class Wallet {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public double balance;
    public String icon;

    // --- THÊM TRƯỜNG NÀY ---
    public String type; // "CASH" hoặc "BANK"

    @ColumnInfo(name = "userId")
    public int userId;

    // Constructor cập nhật thêm type
    public Wallet(String name, double balance, String icon, String type, int userId) {
        this.name = name;
        this.balance = balance;
        this.icon = icon;
        this.type = type;
        this.userId = userId;
    }
}