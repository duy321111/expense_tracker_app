package com.example.expense_tracker_app.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users") // Định nghĩa tên bảng
public class User {

    @PrimaryKey(autoGenerate = true) // Tự động tăng ID
    private int id;

    @ColumnInfo(name = "full_name")
    private String fullName;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "password")
    private String password;

    // Constructor (Room cần 1 constructor rỗng hoặc đầy đủ, ta dùng cái này)
    public User(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }

    // Getter & Setter cho ID (Bắt buộc phải thêm vì có trường id mới)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // ... Giữ nguyên các Getter/Setter cũ của fullName, email, password
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}