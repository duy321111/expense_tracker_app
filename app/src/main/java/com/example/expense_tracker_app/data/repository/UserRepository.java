package com.example.expense_tracker_app.data.repository;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.expense_tracker_app.data.database.AppDatabase;
import com.example.expense_tracker_app.data.database.UserDao;
import com.example.expense_tracker_app.data.datasource.UserLocalDataSource;
import com.example.expense_tracker_app.data.model.User;

public class UserRepository {

    private UserDao userDao;
    private UserLocalDataSource localDataSource;

    // Constructor cho Application (dùng trong ViewModel)
    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        localDataSource = new UserLocalDataSource(application);
    }

    // Constructor cho Context (dùng trong Activity nếu cần)
    public UserRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        userDao = db.userDao();
        localDataSource = new UserLocalDataSource(context);
    }

    // Đăng ký: Trả về true nếu thành công
    public boolean registerUser(User user) {
        try {
            long id = userDao.insertUser(user);
            return id > 0; // Nếu id > 0 nghĩa là insert thành công
        } catch (Exception e) {
            Log.e("UserRepository", "Register error", e);
            return false;
        }
    }

    // Đăng nhập: Kiểm tra DB -> Lưu vào Local
    public boolean checkLogin(String email, String password) {
        try {
            User user = userDao.checkLogin(email, password);
            if (user != null) {
                localDataSource.saveLoggedInUser(user);
                return true;
            }
        } catch (Exception e) {
            Log.e("UserRepository", "Login error", e);
        }
        return false;
    }

    public User getLoggedInUser() {
        return localDataSource.getLoggedInUser();
    }

    public void logout() {
        localDataSource.logout();
    }

    public LiveData<User> getUserById(int userId) {
        return userDao.getUserById(userId);
    }
}