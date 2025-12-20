package com.example.expense_tracker_app.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.expense_tracker_app.data.model.User;

@Dao
public interface UserDao {

    // Trả về ID của dòng vừa thêm (nếu > 0 là thành công)
    @Insert
    long insertUser(User user);

    // Kiểm tra đăng nhập
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User checkLogin(String email, String password);

    // --- CÁC HÀM CHO PHẦN CÀI ĐẶT (PROFILE) ---

    // Lấy thông tin User theo ID
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    LiveData<User> getUserById(int userId);

    // Cập nhật thông tin chung (Tên, Email, Ảnh)
    @Update
    void updateUser(User user);

    // Cập nhật mật khẩu riêng
    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    void updatePassword(int userId, String newPassword);
}