package com.example.expense_tracker_app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.expense_tracker_app.data.database.AppDatabase;
import com.example.expense_tracker_app.data.database.WalletDao;
import com.example.expense_tracker_app.data.repository.UserRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginViewModel extends AndroidViewModel {

    public UserRepository repository;
    private final WalletDao walletDao; // Cần cái này để đếm ví

    // Kết quả trả về: 0 = Lỗi, 1 = Thành công & Có ví, 2 = Thành công & Chưa có ví
    public MutableLiveData<Integer> loginResult = new MutableLiveData<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        // Đảm bảo AppDatabase đã có walletDao() (đã thêm ở bước trước)
        walletDao = AppDatabase.getInstance(application).walletDao();
    }

    public void login(String email, String password) {
        executorService.execute(() -> {
            // 1. Kiểm tra đăng nhập
            boolean success = repository.checkLogin(email, password);

            if (success) {
                // 2. Lấy user vừa đăng nhập
                var loggedUser = repository.getLoggedInUser();
                if (loggedUser != null) {
                    // 3. Kiểm tra xem user này có ví nào chưa
                    int walletCount = walletDao.getWalletCountByUserId(loggedUser.id);
                    if (walletCount > 0) {
                        loginResult.postValue(1); // Đã có ví -> Vào Dashboard
                    } else {
                        loginResult.postValue(2); // Chưa có ví -> Vào AddWallet
                    }
                } else {
                    loginResult.postValue(0); // Lỗi: không thể lấy user
                }
            } else {
                loginResult.postValue(0); // Sai thông tin đăng nhập
            }
        });
    }
}