package com.example.expense_tracker_app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.expense_tracker_app.data.database.AppDatabase;
import com.example.expense_tracker_app.data.database.UserDao;
import com.example.expense_tracker_app.data.model.User;
import com.example.expense_tracker_app.data.repository.UserRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileViewModel extends AndroidViewModel {
    private final UserDao userDao;
    private final UserRepository userRepository; // Thêm Repository
    private final ExecutorService executor;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        userDao = AppDatabase.getInstance(application).userDao();
        userRepository = new UserRepository(application); // Khởi tạo Repository
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<User> getUser(int userId) {
        return userDao.getUserById(userId);
    }

    public void updateUserInfo(User user) {
        executor.execute(() -> userDao.updateUser(user));
    }

    public void changePassword(int userId, String newPass) {
        executor.execute(() -> userDao.updatePassword(userId, newPass));
    }

    // --- THÊM HÀM NÀY: Xử lý đăng xuất ---
    public void logout() {
        userRepository.logout(); // Xóa SharedPreferences
    }
}