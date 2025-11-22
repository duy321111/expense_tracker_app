package com.example.expense_tracker_app.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.expense_tracker_app.data.model.User;

@Dao
public interface UserDao {

    @Insert
    long insertUser(User user); // Trả về ID của dòng vừa thêm

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User checkLogin(String email, String password);
}