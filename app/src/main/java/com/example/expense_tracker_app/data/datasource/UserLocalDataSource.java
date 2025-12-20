package com.example.expense_tracker_app.data.datasource;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.expense_tracker_app.data.model.User;

public class UserLocalDataSource {

    private SharedPreferences prefs;

    public UserLocalDataSource(Context context) {
        prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
    }

    public void saveLoggedInUser(User user) {
        if (user == null) return;
        prefs.edit()
                // --- SỬA Ở ĐÂY: Gọi trực tiếp tên biến ---
                .putString("fullName", user.fullName)
                .putString("email", user.email)
                .putString("password", user.password)
                .putString("profileImagePath", user.profileImagePath)
                // ----------------------------------------
                .apply();
    }

    public User getLoggedInUser() {
        String fullName = prefs.getString("fullName", null);
        String email = prefs.getString("email", null);
        String password = prefs.getString("password", null);
        String imagePath = prefs.getString("profileImagePath", "");

        if (fullName == null || email == null || password == null) return null;

        // Tạo lại User từ dữ liệu đã lưu
        User user = new User(fullName, email, password);
        user.profileImagePath = imagePath;
        return user;
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}