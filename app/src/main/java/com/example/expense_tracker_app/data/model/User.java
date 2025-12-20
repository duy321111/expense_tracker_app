package com.example.expense_tracker_app.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id; // ID tự tăng

    @ColumnInfo(name = "full_name")
    public String fullName;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "password")
    public String password;

    @ColumnInfo(name = "profile_image_path")
    public String profileImagePath;

    // Constructor mặc định cho Room
    public User() { }

    // Constructor đầy đủ (dành cho Room khi đọc dữ liệu)
    public User(int id, String fullName, String email, String password, String profileImagePath) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.profileImagePath = profileImagePath;
    }

    // Constructor dùng để tạo User mới (ID tự sinh, ảnh rỗng)
    @Ignore
    public User(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.profileImagePath = "";
    }
}