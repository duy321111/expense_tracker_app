
package com.example.expense_tracker_app.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.expense_tracker_app.data.database.AppDatabase;
import com.example.expense_tracker_app.data.database.TransactionDao;
import com.example.expense_tracker_app.data.model.Category;
import com.example.expense_tracker_app.data.model.CategoryWithSubcategories;
import com.example.expense_tracker_app.data.model.Subcategory;
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
                long id = transactionDao.insertCategory(category);
                category.id = (int) id;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void insertSubcategory(Subcategory subcategory) {
        executor.execute(() -> {
            try {
                long id = transactionDao.insertSubcategory(subcategory);
                subcategory.id = (int) id;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // 2. Hàm lấy danh sách các danh mục tự tạo từ DB
    public List<Category> getCustomCategories(int userId) {
        try {
            // Vì AppDatabase của bạn có allowMainThreadQueries() nên có thể gọi trực tiếp
            // Nếu không, cần dùng LiveData hoặc chạy trong Thread khác
            return transactionDao.getAllCategories(userId);
        } catch (Exception e) {
            return new ArrayList<>(); // Trả về list rỗng nếu lỗi
        }
    }

    public List<CategoryWithSubcategories> categoriesWithSubcategories(TxType type, int userId) {
        try {
            return transactionDao.getCategoriesWithSubcategories(type, userId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Subcategory findSubcategory(int id) {
        try {
            return transactionDao.findSubcategoryById(id);
        } catch (Exception e) {
            return null;
        }
    }

    public void ensureDefaultCategories(int userId) {
        executor.execute(() -> {
            try {
                if (transactionDao.countCategories() > 0 && transactionDao.countSubcategories() > 0) return;

                // Clear existing to avoid duplicate when fallbackToDestructiveMigration disabled
                // transactionDao is destructive on upgrade, but keep idempotent insert

                // EXPENSE groups
                Category daily = new Category("Chi tiêu hằng ngày", "ic_cat_food");
                daily.type = TxType.EXPENSE;
                daily.userId = userId;
                daily.id = (int) transactionDao.insertCategory(daily);

                Category bills = new Category("Hóa đơn & dịch vụ", "ic_cat_electric");
                bills.type = TxType.EXPENSE;
                bills.userId = userId;
                bills.id = (int) transactionDao.insertCategory(bills);

                Category home = new Category("Nhà cửa & xe", "ic_cat_home");
                home.type = TxType.EXPENSE;
                home.userId = userId;
                home.id = (int) transactionDao.insertCategory(home);

                Category health = new Category("Sức khỏe & học tập", "ic_cat_health");
                health.type = TxType.EXPENSE;
                health.userId = userId;
                health.id = (int) transactionDao.insertCategory(health);

                Category fun = new Category("Giải trí", "ic_cat_travel");
                fun.type = TxType.EXPENSE;
                fun.userId = userId;
                fun.id = (int) transactionDao.insertCategory(fun);

                // INCOME group
                Category income = new Category("Thu nhập", "ic_cat_income");
                income.type = TxType.INCOME;
                income.userId = userId;
                income.id = (int) transactionDao.insertCategory(income);

                // BORROW / LEND groups
                Category borrow = new Category("Đi vay", "ic_cat_money_in");
                borrow.type = TxType.BORROW;
                borrow.userId = userId;
                borrow.id = (int) transactionDao.insertCategory(borrow);

                Category lend = new Category("Cho vay", "ic_cat_money_out");
                lend.type = TxType.LEND;
                lend.userId = userId;
                lend.id = (int) transactionDao.insertCategory(lend);

                // Now insert subcategories
                insertDefaultsForCategory(daily, userId, new String[][]{
                        {"Ăn uống", "ic_cat_food"},
                        {"Cà phê", "ic_cat_coffee"},
                        {"Đi chợ/Siêu thị", "ic_cat_groceries"},
                        {"Di chuyển", "ic_cat_transport"}
                });

                insertDefaultsForCategory(bills, userId, new String[][]{
                        {"Điện", "ic_cat_electric"},
                        {"Nước", "ic_cat_water"},
                        {"Internet", "ic_cat_internet"},
                        {"Điện thoại", "ic_cat_phone"},
                        {"Gas", "ic_cat_gas"},
                        {"TV", "ic_cat_tv"}
                });

                insertDefaultsForCategory(home, userId, new String[][]{
                        {"Thuê nhà", "ic_cat_home"},
                        {"Bảo dưỡng xe", "ic_cat_car_service"},
                        {"Bảo hiểm", "ic_cat_insurance"}
                });

                insertDefaultsForCategory(health, userId, new String[][]{
                        {"Khám sức khỏe", "ic_cat_health"},
                        {"Học tập", "ic_cat_study"},
                        {"Thể thao", "ic_cat_sport"}
                });

                insertDefaultsForCategory(fun, userId, new String[][]{
                        {"Nhạc", "ic_cat_music"},
                        {"Du lịch", "ic_cat_travel"},
                        {"Trò chơi", "ic_cat_gamepad"}
                });

                insertDefaultsForCategory(income, userId, new String[][]{
                        {"Lương", "ic_cat_income"},
                        {"Thưởng", "ic_cat_income"},
                        {"Bán đồ", "ic_cat_income"},
                        {"Khác", "ic_cat_income"}
                });

                insertDefaultsForCategory(borrow, userId, new String[][]{
                        {"Nhận tiền vay", "ic_cat_money_in"}
                });

                insertDefaultsForCategory(lend, userId, new String[][]{
                        {"Cho vay tiền", "ic_cat_money_out"}
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void insertDefaultsForCategory(Category category, int userId, String[][] subs) {
        if (category == null || subs == null) return;
        for (String[] s : subs) {
            if (s.length < 2) continue;
            Subcategory sub = new Subcategory(category.id, s[0], s[1]);
            sub.userId = userId;
            transactionDao.insertSubcategory(sub);
        }
    }

    // Lấy danh sách giao dịch theo user, khoảng ngày (epoch day), và list subcategoryId (dùng cho BudgetDetail)
    public List<Transaction> getTransactionsBySubcategories(int userId, long startEpochDay, long endEpochDay, List<Integer> subcategoryIds) {
        try {
            return transactionDao.getTransactionsBySubcategories(userId, startEpochDay, endEpochDay, subcategoryIds);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // Tính tổng số tiền đã chi theo user, khoảng ngày (epoch day), và list subcategoryId (dùng cho BudgetDetail)
    public double getTotalSpentBySubcategories(int userId, long startEpochDay, long endEpochDay, List<Integer> subcategoryIds) {
        List<Transaction> transactions = getTransactionsBySubcategories(userId, startEpochDay, endEpochDay, subcategoryIds);
        double total = 0;
        for (Transaction t : transactions) {
            total += t.amount;
        }
        return total;
    }
}