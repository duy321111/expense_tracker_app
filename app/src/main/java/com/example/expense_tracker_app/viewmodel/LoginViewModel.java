package com.example.expense_tracker_app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.expense_tracker_app.data.database.AppDatabase;
import com.example.expense_tracker_app.data.database.UserDao;
import com.example.expense_tracker_app.data.database.WalletDao;
import com.example.expense_tracker_app.data.model.User;
import com.example.expense_tracker_app.data.model.Wallet;
import com.example.expense_tracker_app.data.repository.UserRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginViewModel extends AndroidViewModel {

    public UserRepository repository;

    private final UserDao userDao;
    private final WalletDao walletDao;

    // 0 = Lỗi, 1 = Thành công & Có ví, 2 = Thành công & Chưa có ví
    public MutableLiveData<Integer> loginResult = new MutableLiveData<>();

    // ✅ trả về User đang login (đúng id từ DB)
    public MutableLiveData<User> loggedUser = new MutableLiveData<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);

        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        walletDao = db.walletDao();
    }

    public void login(String email, String password) {
        executorService.execute(() -> {

            // ✅ LẤY USER THẬT TỪ DB (có id > 0)
            User user = userDao.checkLogin(email, password);

            if (user == null) {
                loginResult.postValue(0);
                return;
            }

            // post user lên UI để Login.java lưu session
            loggedUser.postValue(user);

            // ✅ ĐẾM VÍ THEO USER (đúng theo người đăng nhập)
            List<Wallet> wallets = walletDao.getWalletsByUser(user.id);
            int walletCount = (wallets == null) ? 0 : wallets.size();

            if (walletCount > 0) {
                loginResult.postValue(1);
            } else {
                loginResult.postValue(2);
            }
        });
    }
}
