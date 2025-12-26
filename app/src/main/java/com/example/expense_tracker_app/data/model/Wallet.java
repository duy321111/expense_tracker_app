package com.example.expense_tracker_app.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wallets")
public class Wallet {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;   // THÊM

    public String name;
    public double balance;
    public String icon;

    public Wallet(int userId, String name, double balance, String icon) {
        this.userId = userId;
        this.name = name;
        this.balance = balance;
        this.icon = icon;
    }

    @Override
    public String toString() {
        return name;
    }
}