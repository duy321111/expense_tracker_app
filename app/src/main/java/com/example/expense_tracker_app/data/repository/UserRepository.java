package com.example.expense_tracker_app.data.repository;

import android.content.Context;
import com.example.expense_tracker_app.data.database.AppDatabase;
import com.example.expense_tracker_app.data.database.UserDao;
import com.example.expense_tracker_app.data.datasource.UserLocalDataSource;
import com.example.expense_tracker_app.data.model.User;

public class UserRepository {

    private UserDao userDao; // Dùng DAO thay vì DatabaseHandler
    private UserLocalDataSource localDataSource;

    public UserRepository(Context context) {
        // Khởi tạo Room Database
        AppDatabase db = AppDatabase.getInstance(context);
        userDao = db.userDao();

        localDataSource = new UserLocalDataSource(context);
    }

    // Room: insert user
    public boolean registerUser(User user) {
        try {
            long id = userDao.insertUser(user);
            return id > 0; // Nếu id > 0 nghĩa là thêm thành công
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Room: check login
    public boolean checkLogin(String email, String password) {
        try {
            // Room tự map dữ liệu vào object User, không cần Cursor nữa
            User user = userDao.checkLogin(email, password);

            if (user != null) {
                // Lưu vào SharedPreferences như cũ
                localDataSource.saveLoggedInUser(user);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public User getLoggedInUser() {
        return localDataSource.getLoggedInUser();
    }

    public void logout() {
        localDataSource.logout();
    }
}