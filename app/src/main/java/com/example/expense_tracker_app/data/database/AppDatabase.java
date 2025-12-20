package com.example.expense_tracker_app.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.expense_tracker_app.data.model.Budget;
import com.example.expense_tracker_app.data.model.Category;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.User;
import com.example.expense_tracker_app.data.model.Wallet; // Import Wallet
import com.example.expense_tracker_app.data.model.converter.Converters;
import com.example.expense_tracker_app.data.model.converter.StringListConverter;

// NHỚ: Tăng version lên 6
@Database(entities = {User.class, Budget.class, Transaction.class, Category.class, Wallet.class}, version = 6)
@TypeConverters({StringListConverter.class, Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "expense_app_db";

    public abstract UserDao userDao();
    public abstract BudgetDao budgetDao();
    public abstract TransactionDao transactionDao();
    public abstract WalletDao walletDao(); // Bắt buộc có dòng này

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}