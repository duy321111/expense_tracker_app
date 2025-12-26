package com.example.expense_tracker_app.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete; // Thêm import này
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.expense_tracker_app.data.model.Wallet;
import java.util.List;

@Dao
public interface WalletDao {
    @Insert
    void insertWallet(Wallet wallet);

    // Lấy tất cả ví của một user theo userId
    @Query("SELECT * FROM wallets WHERE userId = :userId")
    LiveData<List<Wallet>> getWalletsByUserId(int userId);

    // Lấy tất cả ví (giữ lại cho compatibility)
    @Query("SELECT * FROM wallets")
    LiveData<List<Wallet>> getAllWallets();

    // Đếm ví của một user cụ thể
    @Query("SELECT COUNT(*) FROM wallets WHERE userId = :userId")
    int getWalletCountByUserId(int userId);

    // Đếm tất cả ví
    @Query("SELECT COUNT(*) FROM wallets")
    int getWalletCount();

    @Query("UPDATE wallets SET balance = balance + :amount WHERE name = :walletName")
    void updateBalance(String walletName, double amount);

    // --- SỬA LỖI: Thêm hàm Delete ---
    @Delete
    void deleteWallet(Wallet wallet);
}