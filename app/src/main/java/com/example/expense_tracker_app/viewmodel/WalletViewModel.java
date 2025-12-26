package com.example.expense_tracker_app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import com.example.expense_tracker_app.data.database.AppDatabase;
import com.example.expense_tracker_app.data.database.WalletDao;
import com.example.expense_tracker_app.data.model.Wallet;
import com.example.expense_tracker_app.data.repository.UserRepository;
import java.util.List;
import java.util.concurrent.Executors;

public class WalletViewModel extends AndroidViewModel {
    private final WalletDao walletDao;
    private final UserRepository userRepository;
    public LiveData<List<Wallet>> wallets; // Danh sách ví
    public LiveData<Double> totalBalance;  // Tổng tiền

    public WalletViewModel(@NonNull Application application) {
        super(application);
        walletDao = AppDatabase.getInstance(application).walletDao();
        userRepository = new UserRepository(application);
        
        // Lấy user đang đăng nhập và load ví của user đó
        loadWalletsForCurrentUser();
    }
    
    private void loadWalletsForCurrentUser() {
        // Lấy user đang đăng nhập từ SharedPreferences
        var loggedUser = userRepository.getLoggedInUser();
        if (loggedUser != null) {
            wallets = walletDao.getWalletsByUserId(loggedUser.id);
        } else {
            // Nếu không có user đăng nhập, lấy tất cả ví (fallback)
            wallets = walletDao.getAllWallets();
        }
        
        // Tự động tính tổng tiền mỗi khi danh sách ví thay đổi
        totalBalance = Transformations.map(wallets, walletList -> {
            double total = 0;
            for (Wallet w : walletList) {
                total += w.balance;
            }
            return total;
        });
    }

    public void deleteWallet(Wallet wallet) {
        Executors.newSingleThreadExecutor().execute(() -> {
            walletDao.deleteWallet(wallet);
        });
    }
}