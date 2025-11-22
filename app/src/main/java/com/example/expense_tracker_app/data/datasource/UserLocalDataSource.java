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
        prefs.edit()
                .putString("fullName", user.getFullName())
                .putString("email", user.getEmail())
                .putString("password", user.getPassword())
                .apply();
    }

    public User getLoggedInUser() {
        String fullName = prefs.getString("fullName", null);
        String email = prefs.getString("email", null);
        String password = prefs.getString("password", null);

        if (fullName == null || email == null || password == null) return null;

        return new User(fullName, email, password);
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}
