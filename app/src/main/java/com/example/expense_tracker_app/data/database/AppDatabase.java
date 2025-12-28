package com.example.expense_tracker_app.data.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.expense_tracker_app.data.model.Budget;
import com.example.expense_tracker_app.data.model.Category;
import com.example.expense_tracker_app.data.model.Subcategory;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.User;
import com.example.expense_tracker_app.data.model.Wallet;
import com.example.expense_tracker_app.data.model.converter.Converters;
import com.example.expense_tracker_app.data.model.converter.StringListConverter;

// --- SỬA: Tăng version lên 10 ---
@Database(entities = {User.class, Budget.class, Transaction.class, Category.class, Subcategory.class, Wallet.class}, version = 10)
@TypeConverters({StringListConverter.class, Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "expense_app_db";

    public abstract UserDao userDao();
    public abstract BudgetDao budgetDao();
    public abstract TransactionDao transactionDao();
    public abstract WalletDao walletDao();


    private static volatile AppDatabase INSTANCE;

    // Migration cũ (giữ nguyên)
    static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE wallets ADD COLUMN type TEXT DEFAULT 'BANK'");
        }
    };

    // --- MỚI: Migration thêm cột excludeFromReport ---
    static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Thêm cột excludeFromReport (INTEGER 0 hoặc 1), mặc định là 0 (tính vào báo cáo)
            database.execSQL("ALTER TABLE transactions ADD COLUMN excludeFromReport INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            .allowMainThreadQueries()
                            .addMigrations(MIGRATION_8_9, MIGRATION_9_10) // Đăng ký thêm migration mới
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}