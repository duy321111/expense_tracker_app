package com.example.expense_tracker_app.data.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories") // 1. Định nghĩa là bảng
public class Category {

    @PrimaryKey(autoGenerate = true) // 2. Khóa chính tự tăng
    public int id;

    public String name;
    public TxType type;
    public String icon;
    public int userId; // Thêm userId để lọc theo user

    // 3. Constructor mặc định (Bắt buộc cho Room)
    public Category() { }

    // Constructor dùng để tạo nhanh (cho code Java)
    @Ignore
    public Category(String name, String icon) {
        this.name = name;
        this.icon = icon;
        this.type = TxType.EXPENSE; // Mặc định
    }

    @Ignore
    public Category(int id, String name, TxType type, String icon) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.icon = icon;
    }

    @Ignore
    public Category(String name) {
        this.name = name;
        this.type = TxType.EXPENSE;
        this.icon = "ic_category";
    }

    @Override
    public String toString() {
        return name;
    }
}