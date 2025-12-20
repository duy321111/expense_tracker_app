package com.example.expense_tracker_app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.expense_tracker_app.data.database.AppDatabase;
import com.example.expense_tracker_app.data.database.WalletDao;
import com.example.expense_tracker_app.data.model.Category;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.TxType;
import com.example.expense_tracker_app.data.repository.TransactionRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;

public class AddTxViewModel extends AndroidViewModel {

    private final TransactionRepository repo;
    private final WalletDao walletDao; // 1. Khai báo WalletDao

    // Các biến LiveData binding với UI
    public final MutableLiveData<TxType> type = new MutableLiveData<>(TxType.EXPENSE);
    public final MutableLiveData<Category> category = new MutableLiveData<>();
    public final MutableLiveData<String> method = new MutableLiveData<>("Tiền mặt"); // Mặc định là Tiền mặt
    public final MutableLiveData<String> amount = new MutableLiveData<>("");
    public final MutableLiveData<LocalDate> date = new MutableLiveData<>(LocalDate.now());
    public final MutableLiveData<Boolean> done = new MutableLiveData<>(false);
    public final MutableLiveData<String> note = new MutableLiveData<>("");
    public final MutableLiveData<String> location = new MutableLiveData<>("");
    public final MutableLiveData<Boolean> excludeReport = new MutableLiveData<>(false);
    public final MutableLiveData<String> imagePath = new MutableLiveData<>("");

    public AddTxViewModel(@NonNull Application application) {
        super(application);
        repo = new TransactionRepository(application);
        // 2. Khởi tạo WalletDao từ Database
        walletDao = AppDatabase.getInstance(application).walletDao();
    }

    public List<Category> categories(){
        return repo.categoriesBy(type.getValue());
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
        if (finalCat == null) finalCat = new Category(type.getValue().name(), "ic_category");

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
                amountVal,
                method.getValue(), // Đây là tên Ví ("Tiền mặt" hoặc "Chuyển khoản")
                date.getValue(),
                finalNote,
                locationVal,
                imagePathVal
        );

        // --- LOGIC QUAN TRỌNG: Cập nhật Ví & Lưu Giao dịch ---
        final long finalAmount = amountVal;

        Executors.newSingleThreadExecutor().execute(() -> {
            // 1. Lưu giao dịch vào bảng transactions
            repo.insertTransaction(newTx);

            // 2. Tính toán số tiền thay đổi (Dương hoặc Âm)
            double changeAmount = 0;
            TxType currentType = type.getValue();

            if (currentType == TxType.EXPENSE) {
                changeAmount = -finalAmount; // Chi tiêu -> Trừ tiền
            } else if (currentType == TxType.INCOME) {
                changeAmount = finalAmount;  // Thu nhập -> Cộng tiền
            } else if (currentType == TxType.BORROW) {
                changeAmount = finalAmount;  // Đi vay -> Tiền vào ví -> Cộng tiền
            } else if (currentType == TxType.LEND) {
                changeAmount = -finalAmount; // Cho vay -> Tiền ra khỏi ví -> Trừ tiền
            }

            // 3. Cập nhật vào Ví (dựa theo tên ví: "Tiền mặt" hoặc "Chuyển khoản")
            // method.getValue() sẽ trả về đúng chuỗi mà người dùng chọn ở UI
            if (changeAmount != 0) {
                walletDao.updateBalance(method.getValue(), changeAmount);
            }

            // 4. Báo xong để UI quay về màn hình trước
            done.postValue(true);
        });
    }

    public void addNewCategory(String name, String icon) {
        Category newCat = new Category(name, icon);
        new Thread(() -> repo.insertCategory(newCat)).start();
    }

    public List<Category> getCustomCategories() {
        return repo.getCustomCategories();
    }
}