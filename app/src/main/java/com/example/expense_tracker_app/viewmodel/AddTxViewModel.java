package com.example.expense_tracker_app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.expense_tracker_app.data.database.AppDatabase;
import com.example.expense_tracker_app.data.database.WalletDao;
import com.example.expense_tracker_app.data.model.Category;
import com.example.expense_tracker_app.data.model.CategoryWithSubcategories;
import com.example.expense_tracker_app.data.model.Subcategory;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.TxType;
import com.example.expense_tracker_app.data.model.User;
import com.example.expense_tracker_app.data.repository.TransactionRepository;
import com.example.expense_tracker_app.data.repository.UserRepository;


import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class AddTxViewModel extends AndroidViewModel {

    private final TransactionRepository repo;
    private final WalletDao walletDao;
    private final UserRepository userRepository;

    // Các biến LiveData binding với UI
    public final MutableLiveData<TxType> type = new MutableLiveData<>(TxType.EXPENSE);
    public final MutableLiveData<Category> category = new MutableLiveData<>();
    public final MutableLiveData<Subcategory> subcategory = new MutableLiveData<>();
    public final MutableLiveData<Integer> subcategoryId = new MutableLiveData<>(0);
    public final MutableLiveData<String> method = new MutableLiveData<>("Tiền mặt");
    public final MutableLiveData<String> amount = new MutableLiveData<>("");
    public final MutableLiveData<LocalDate> date = new MutableLiveData<>(LocalDate.now());
    public final MutableLiveData<Boolean> done = new MutableLiveData<>(false);
    public final MutableLiveData<String> note = new MutableLiveData<>("");
    public final MutableLiveData<String> location = new MutableLiveData<>("");
    public final MutableLiveData<Boolean> excludeReport = new MutableLiveData<>(false);
    public final MutableLiveData<String> imagePath = new MutableLiveData<>("");
    public final MutableLiveData<List<CategoryWithSubcategories>> categories = new MutableLiveData<>();

    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    public AddTxViewModel(@NonNull Application application) {
        super(application);
        repo = new TransactionRepository(application);
        // Khởi tạo WalletDao
        walletDao = AppDatabase.getInstance(application).walletDao();
        // Khởi tạo UserRepository để lấy thông tin user
        userRepository = new UserRepository(application);

        repo.ensureDefaultCategories(getUserId());
        refreshCategories();
    }

    public void refreshCategories() {
        TxType current = type.getValue() == null ? TxType.EXPENSE : type.getValue();
        ioExecutor.execute(() -> {
            List<CategoryWithSubcategories> data = repo.categoriesWithSubcategories(current, getUserId());
            categories.postValue(data);
        });
    }

    public void submit() {
        long amountVal = 0;
        try {
            String cleanAmount = amount.getValue();
            if (cleanAmount == null || cleanAmount.isEmpty()) return;
            // Xóa hết ký tự không phải số để parse
            amountVal = Long.parseLong(cleanAmount.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) { return; }

        Category finalCat = category.getValue();
        // Fallback icon mặc định nếu chưa chọn category
        if (finalCat == null) finalCat = new Category(type.getValue().name(), "ic_category");

        Subcategory pickedSub = subcategory.getValue();
        int pickedSubId = pickedSub != null ? pickedSub.id : 0;
        String pickedSubName = pickedSub != null ? pickedSub.name : "";
        String pickedSubIcon = pickedSub != null && pickedSub.icon != null ? pickedSub.icon : finalCat.icon;

        String finalNote = note.getValue();
        if (Boolean.TRUE.equals(excludeReport.getValue())) {
            finalNote += " [Không tính báo cáo]";
        }

        String locationVal = location.getValue();
        String imagePathVal = imagePath.getValue();
        if (imagePathVal == null) imagePathVal = "";

        // Tạo đối tượng Transaction
        int userId = getUserId();
        LocalDate txDate = date.getValue();
        int month = txDate != null ? txDate.getMonthValue() : LocalDate.now().getMonthValue();
        int year = txDate != null ? txDate.getYear() : LocalDate.now().getYear();
        Transaction newTx = new Transaction(
            0, userId,
            type.getValue(),
            finalCat,
            pickedSubId,
            pickedSubName,
            pickedSubIcon,
            amountVal,
            method.getValue(),
            date.getValue(),
            finalNote,
            locationVal,
            imagePathVal
        );

        // --- LOGIC CẬP NHẬT VÍ ---
        final long finalAmount = amountVal;

        Executors.newSingleThreadExecutor().execute(() -> {
            // 1. Lưu giao dịch
            repo.insertTransaction(newTx);

            // 2. Tạm thời bỏ cập nhật spent_amount cho các budget liên quan
            // BudgetSpentUpdater.updateAllBudgetsSpent(getApplication(), userId, month, year);

            // 3. Tính toán thay đổi số dư
            double changeAmount = 0;
            TxType currentType = type.getValue();
            if (currentType == TxType.EXPENSE) {
                changeAmount = -finalAmount;
            } else if (currentType == TxType.INCOME) {
                changeAmount = finalAmount;
            } else if (currentType == TxType.BORROW) {
                changeAmount = finalAmount;
            } else if (currentType == TxType.LEND) {
                changeAmount = -finalAmount;
            }
            if (changeAmount != 0) {
                walletDao.updateBalance(method.getValue(), changeAmount);
            }

            // 4. Hoàn tất
            done.postValue(true);
        });
    }

    public void addNewCategory(String name, String icon) {
        Category newCat = new Category(name, icon);
        newCat.userId = getUserId(); // Gán userId cho category
        ioExecutor.execute(() -> {
            repo.insertCategory(newCat);
            refreshCategories();
        });
    }

    public List<Category> getCustomCategories() { // legacy use
        return repo.getCustomCategories(getUserId());
    }

    public void addNewSubcategory(int categoryId, String name, String icon) {
        Subcategory sub = new Subcategory(categoryId, name, icon);
        sub.userId = getUserId(); // Gán userId cho subcategory
        ioExecutor.execute(() -> {
            repo.insertSubcategory(sub);
            // Gọi refreshCategories sau khi insert thành công
            refreshCategories();
        });
    }

    private int getUserId() {
        try {
            User loggedUser = userRepository.getLoggedInUser();
            return loggedUser != null ? loggedUser.id : 1; // Default userId = 1 nếu không tìm thấy
        } catch (Exception e) {
            return 1; // Default userId = 1 nếu có lỗi
        }
    }
}