package com.example.expense_tracker_app.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wallets")
public class Wallet {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public double balance;
    public String icon; // Icon ví (ví dụ: ic_wallet, ic_cash)

    // Constructor dùng để tạo ví mới
    public Wallet(String name, double balance, String icon) {
        this.name = name;
        this.balance = balance;
        this.icon = icon;
    }
}