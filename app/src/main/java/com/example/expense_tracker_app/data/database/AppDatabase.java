package com.example.expense_tracker_app.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.expense_tracker_app.data.model.User;
import com.example.expense_tracker_app.data.model.Budget;
import com.example.expense_tracker_app.data.database.BudgetDao;

@Database(entities = {User.class, Budget.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "expense_app_db";
    public abstract UserDao userDao();
    public abstract BudgetDao budgetDao();

    private static volatile AppDatabase INSTANCE;


    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            // LƯU Ý: Cho phép chạy trên Main Thread để bạn dễ test (thực tế nên dùng Thread khác)
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}