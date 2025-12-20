package com.example.expense_tracker_app.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.expense_tracker_app.data.database.AppDatabase;
import com.example.expense_tracker_app.data.database.TransactionDao;
import com.example.expense_tracker_app.data.model.Category;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.TxType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.LocalDate;

public class TransactionRepository {
    private final TransactionDao transactionDao;
    private final ExecutorService executor;

    public TransactionRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        transactionDao = db.transactionDao();
        executor = Executors.newSingleThreadExecutor();
    }

    // --- CÁC HÀM XỬ LÝ TRANSACTION (GIAO DỊCH) ---

    public void insertTransaction(Transaction transaction) {
        executor.execute(() -> transactionDao.insertTransaction(transaction));
    }

    public LiveData<List<Transaction>> getAllTransactions(int userId) {
        return transactionDao.getAllTransactions(userId);
    }

    public LiveData<List<Transaction>> getTransactionsByMonth(int userId, LocalDate date) {
        LocalDate startDate = date.withDayOfMonth(1);
        LocalDate endDate = date.withDayOfMonth(date.lengthOfMonth());
        return transactionDao.getTransactionsByDateRange(userId, startDate, endDate);
    }

    // --- CÁC HÀM XỬ LÝ CATEGORY (DANH MỤC MỚI) ---

    // 1. Hàm lưu danh mục mới vào DB
    public void insertCategory(Category category) {
        executor.execute(() -> {
            try {
                transactionDao.insertCategory(category);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // 2. Hàm lấy danh sách các danh mục tự tạo từ DB
    public List<Category> getCustomCategories() {
        try {
            // Vì AppDatabase của bạn có allowMainThreadQueries() nên có thể gọi trực tiếp
            // Nếu không, cần dùng LiveData hoặc chạy trong Thread khác
            return transactionDao.getAllCategories();
        } catch (Exception e) {
            return new ArrayList<>(); // Trả về list rỗng nếu lỗi
        }
    }

    // --- HÀM PHỤ (Hardcode cho các danh mục mặc định) ---
    public List<Category> categoriesBy(TxType type) {
        List<Category> list = new ArrayList<>();
        if (type == TxType.EXPENSE) {
            list.add(new Category("Ăn uống", "ic_cat_food"));
            list.add(new Category("Di chuyển", "ic_cat_transport"));
            list.add(new Category("Mua sắm", "ic_cat_groceries"));
            list.add(new Category("Hóa đơn", "ic_cat_electric"));
        } else if (type == TxType.INCOME) {
            list.add(new Category("Lương", "ic_income")); // Sửa lại icon cho khớp drawable
            list.add(new Category("Thưởng", "ic_income"));
            list.add(new Category("Bán đồ", "ic_income"));
        }
        return list;
    }
}