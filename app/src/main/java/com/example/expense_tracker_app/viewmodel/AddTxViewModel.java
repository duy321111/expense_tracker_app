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
import com.example.expense_tracker_app.data.repository.TransactionRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class AddTxViewModel extends AndroidViewModel {

    private final TransactionRepository repo;
    private final WalletDao walletDao;

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

        repo.ensureDefaultCategories();
        refreshCategories();
    }

    public void refreshCategories() {
        TxType current = type.getValue() == null ? TxType.EXPENSE : type.getValue();
        ioExecutor.execute(() -> {
            List<CategoryWithSubcategories> data = repo.categoriesWithSubcategories(current);
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
        Transaction newTx = new Transaction(
            0, 1,
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

            // 2. Tính toán thay đổi số dư
            double changeAmount = 0;
            TxType currentType = type.getValue();

            // Logic quan trọng bạn yêu cầu:
            if (currentType == TxType.EXPENSE) {
                // Chi tiêu: Tiền đi ra -> Trừ
                changeAmount = -finalAmount;
            } else if (currentType == TxType.INCOME) {
                // Thu nhập: Tiền đi vào -> Cộng
                changeAmount = finalAmount;
            } else if (currentType == TxType.BORROW) {
                // Đi vay: Mình cầm tiền về -> Ví tăng -> Cộng
                changeAmount = finalAmount;
            } else if (currentType == TxType.LEND) {
                // Cho vay: Mình đưa tiền cho người khác -> Ví giảm -> Trừ
                changeAmount = -finalAmount;
            }

            // 3. Cập nhật vào Ví (nếu có thay đổi)
            if (changeAmount != 0) {
                walletDao.updateBalance(method.getValue(), changeAmount);
            }

            // 4. Hoàn tất
            done.postValue(true);
        });
    }

    public void addNewCategory(String name, String icon) {
        Category newCat = new Category(name, icon);
        ioExecutor.execute(() -> {
            repo.insertCategory(newCat);
            refreshCategories();
        });
    }

    public List<Category> getCustomCategories() { // legacy use
        return repo.getCustomCategories();
    }

    public void addNewSubcategory(int categoryId, String name, String icon) {
        Subcategory sub = new Subcategory(categoryId, name, icon);
        ioExecutor.execute(() -> {
            repo.insertSubcategory(sub);
            refreshCategories();
        });
    }
}