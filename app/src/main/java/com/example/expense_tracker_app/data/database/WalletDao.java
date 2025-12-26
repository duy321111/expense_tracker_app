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

    @Query("SELECT * FROM wallets")
    LiveData<List<Wallet>> getAllWallets();

    @Query("SELECT COUNT(*) FROM wallets")
    int getWalletCount();

    @Query("UPDATE wallets SET balance = balance + :amount WHERE name = :walletName")
    void updateBalance(String walletName, double amount);

    // --- SỬA LỖI: Thêm hàm Delete ---
    @Delete
    void deleteWallet(Wallet wallet);

    @Query("SELECT * FROM wallets WHERE userId = :userId")
    List<Wallet> getWalletsByUser(int userId);

    @Query("SELECT SUM(balance) FROM wallets WHERE userId = :userId")
    Double getTotalBalanceByUser(int userId);


}