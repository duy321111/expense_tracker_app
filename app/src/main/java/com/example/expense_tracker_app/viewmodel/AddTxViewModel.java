package com.example.expense_tracker_app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.expense_tracker_app.data.database.AppDatabase;
import com.example.expense_tracker_app.data.database.WalletDao;
import com.example.expense_tracker_app.data.model.Category;
import com.example.expense_tracker_app.data.model.CategoryWithSubcategories;
import com.example.expense_tracker_app.data.model.Subcategory;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.TxType;
import com.example.expense_tracker_app.data.model.User;
import com.example.expense_tracker_app.data.model.Wallet;
import com.example.expense_tracker_app.data.repository.TransactionRepository;
import com.example.expense_tracker_app.data.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddTxViewModel extends AndroidViewModel {

    private final TransactionRepository repo;
    private final WalletDao walletDao;
    private final UserRepository userRepository;

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

    public final MutableLiveData<Boolean> hasCashWallet = new MutableLiveData<>(false);
    public final MutableLiveData<Boolean> hasBankWallet = new MutableLiveData<>(false);
    private LiveData<List<Wallet>> walletListLiveData;

    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    public AddTxViewModel(@NonNull Application application) {
        super(application);
        repo = new TransactionRepository(application);
        walletDao = AppDatabase.getInstance(application).walletDao();
        userRepository = new UserRepository(application);

        repo.ensureDefaultCategories(getUserId());
        refreshCategories();
        checkWallets();
    }

    private void checkWallets() {
        walletListLiveData = walletDao.getWalletsByUserId(getUserId());
        walletListLiveData.observeForever(wallets -> {
            boolean cash = false;
            boolean bank = false;
            if (wallets != null) {
                for (Wallet w : wallets) {
                    if ("CASH".equals(w.type)) cash = true;
                    else bank = true;
                }
            }
            hasCashWallet.setValue(cash);
            hasBankWallet.setValue(bank);

            if (cash && !bank) method.setValue("Tiền mặt");
            else if (!cash && bank) method.setValue("Chuyển khoản");
        });
    }

    public void refreshCategories() {
        TxType current = type.getValue() == null ? TxType.EXPENSE : type.getValue();
        ioExecutor.execute(() -> {
            List<CategoryWithSubcategories> data = repo.categoriesWithSubcategories(current, getUserId());
            categories.postValue(data);
        });
    }

    public void resetInput() {
        amount.setValue("");
        note.setValue("");
        location.setValue("");
        imagePath.setValue("");
        excludeReport.setValue(false);
        date.setValue(LocalDate.now());
        done.setValue(false);
    }

    public void submit() {
        long amountVal = 0;
        try {
            String cleanAmount = amount.getValue();
            if (cleanAmount == null || cleanAmount.isEmpty()) return;
            amountVal = Long.parseLong(cleanAmount.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) { return; }

        Category finalCat = category.getValue();
        if (finalCat == null) finalCat = new Category(type.getValue().name(), "ic_category");

        TxType finalType = type.getValue();
        if (finalCat.type != null) {
            finalType = finalCat.type;
        }

        Subcategory pickedSub = subcategory.getValue();
        int pickedSubId = pickedSub != null ? pickedSub.id : 0;
        String pickedSubName = pickedSub != null ? pickedSub.name : "";
        String pickedSubIcon = pickedSub != null && pickedSub.icon != null ? pickedSub.icon : finalCat.icon;

        String finalNote = note.getValue();
        String locationVal = location.getValue();
        String imagePathVal = imagePath.getValue();
        if (imagePathVal == null) imagePathVal = "";
        boolean isExcluded = Boolean.TRUE.equals(excludeReport.getValue());

        int userId = getUserId();

        Transaction newTx = new Transaction(
                0, userId,
                finalType,
                finalCat,
                pickedSubId,
                pickedSubName,
                pickedSubIcon,
                amountVal,
                method.getValue(),
                date.getValue(),
                finalNote,
                locationVal,
                imagePathVal,
                isExcluded
        );

        final long finalAmount = amountVal;
        final TxType currentType = finalType;

        Executors.newSingleThreadExecutor().execute(() -> {
            repo.insertTransaction(newTx);
            double changeAmount = 0;

            if (currentType == TxType.EXPENSE) {
                changeAmount = -finalAmount;
            } else if (currentType == TxType.INCOME) {
                changeAmount = finalAmount;
            } else if (currentType == TxType.BORROW) {
                changeAmount = finalAmount;
            } else if (currentType == TxType.LEND) {
                changeAmount = -finalAmount;
            } else if (currentType == TxType.DEBT_COLLECTION) {
                changeAmount = finalAmount;
            } else if (currentType == TxType.LOAN_REPAYMENT) {
                changeAmount = -finalAmount;
            }

            if (changeAmount != 0) {
                walletDao.updateBalance(method.getValue(), changeAmount);
            }
            done.postValue(true);
        });
    }

    // --- HÀM MỚI: Thêm nhóm danh mục đơn giản ---
    public void addNewGroup(String name) {
        // Mặc định icon chung chung vì user không chọn
        Category newCat = new Category(name, "ic_category");
        newCat.userId = getUserId();
        // Quan trọng: Gán đúng Type (Thu/Chi) đang chọn
        newCat.type = type.getValue();

        ioExecutor.execute(() -> {
            // Dùng Sync để đảm bảo lưu xong mới Refresh
            repo.insertCategorySync(newCat);
            refreshCategories();
        });
    }
    // --------------------------------------------

    public void addNewCategory(String name, String icon, TxType txType) {
        Category newCat = new Category(name, icon);
        newCat.userId = getUserId();
        newCat.type = txType;
        ioExecutor.execute(() -> {
            repo.insertCategorySync(newCat);
            refreshCategories();
        });
    }

    public List<Category> getCustomCategories() {
        return repo.getCustomCategories(getUserId());
    }

    public void addNewSubcategory(int categoryId, String name, String icon) {
        Subcategory sub = new Subcategory(categoryId, name, icon);
        sub.userId = getUserId();
        ioExecutor.execute(() -> {
            repo.insertSubcategorySync(sub);
            refreshCategories();
        });
    }

    public void addNewSubcategoryToGroup(String groupName, String subName, String icon) {
        ioExecutor.execute(() -> {
            List<Category> cats = repo.getCustomCategories(getUserId());
            int parentId = -1;
            for (Category c : cats) {
                if (c.name.contains(groupName) || groupName.contains(c.name)) {
                    parentId = c.id;
                    break;
                }
            }
            if (parentId == -1) {
                Category newGroup = new Category(groupName, "ic_category");
                newGroup.userId = getUserId();
                newGroup.type = type.getValue();
                repo.insertCategorySync(newGroup);
                parentId = newGroup.id;
            }
            Subcategory sub = new Subcategory(parentId, subName, icon);
            sub.userId = getUserId();
            repo.insertSubcategorySync(sub);
            refreshCategories();
        });
    }

    private int getUserId() {
        try {
            User loggedUser = userRepository.getLoggedInUser();
            return loggedUser != null ? loggedUser.id : 1;
        } catch (Exception e) {
            return 1;
        }
    }
}