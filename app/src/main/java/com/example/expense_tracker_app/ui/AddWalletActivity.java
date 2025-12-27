package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.database.AppDatabase;
import com.example.expense_tracker_app.data.model.User;
import com.example.expense_tracker_app.data.model.Wallet;
import com.example.expense_tracker_app.data.repository.UserRepository;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.concurrent.Executors;

public class AddWalletActivity extends AppCompatActivity {

    private boolean hasCashWallet = false;
    private boolean hasBankWallet = false;
    private RadioButton radioCash, radioTransfer;
    private Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wallet);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_add_wallet);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 1. Ánh xạ View
        RadioGroup radioGroup = findViewById(R.id.radioGroupWalletType);
        radioCash = findViewById(R.id.radio_cash);
        radioTransfer = findViewById(R.id.radio_transfer);

        EditText etInitialBalance = findViewById(R.id.et_initial_balance);
        btnCreate = findViewById(R.id.btn_create_wallet);

        // 2. Lấy user hiện tại
        UserRepository userRepository = new UserRepository(this);
        User currentUser = userRepository.getLoggedInUser();

        if (currentUser == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy user", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- 3. LOGIC MỚI: KIỂM TRA VÍ ĐÃ TỒN TẠI CHƯA ---
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        db.walletDao().getWalletsByUserId(currentUser.id).observe(this, new Observer<List<Wallet>>() {
            @Override
            public void onChanged(List<Wallet> wallets) {
                hasCashWallet = false;
                hasBankWallet = false;

                if (wallets != null) {
                    for (Wallet w : wallets) {
                        if ("CASH".equals(w.type)) hasCashWallet = true;
                            // Các ví cũ (type null) hoặc ví mới type BANK đều tính là ví ngân hàng
                        else hasBankWallet = true;
                    }
                }
                updateUIState();
            }
        });

        // Xử lý sự kiện bấm nút Tạo Ví
        btnCreate.setOnClickListener(v -> {
            String balanceStr = etInitialBalance.getText().toString().trim();

            double initialBalance = 0;
            if (!balanceStr.isEmpty()) {
                try {
                    initialBalance = Double.parseDouble(balanceStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(AddWalletActivity.this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Logic xác định Tên, Type và Icon
            String walletName;
            String type;
            String icon;

            int selectedId = radioGroup.getCheckedRadioButtonId();

            if (selectedId == R.id.radio_cash) {
                // Kiểm tra lại lần cuối cho chắc
                if (hasCashWallet) {
                    Toast.makeText(this, "Bạn đã có ví tiền mặt rồi!", Toast.LENGTH_SHORT).show();
                    return;
                }
                walletName = "Tiền mặt";
                type = "CASH";
                icon = "ic_cash";
            } else {
                if (hasBankWallet) {
                    Toast.makeText(this, "Bạn đã có ví chuyển khoản rồi!", Toast.LENGTH_SHORT).show();
                    return;
                }
                walletName = "Chuyển khoản";
                type = "BANK";
                icon = "ic_wallet";
            }

            // Lưu vào Database
            Wallet newWallet = new Wallet(walletName, initialBalance, icon, type, currentUser.id);

            Executors.newSingleThreadExecutor().execute(() -> {
                db.walletDao().insertWallet(newWallet);
                runOnUiThread(() -> {
                    Toast.makeText(AddWalletActivity.this, "Đã thêm ví: " + newWallet.name, Toast.LENGTH_SHORT).show();
                    boolean isFirstRun = getIntent().getBooleanExtra("IS_FIRST_RUN", false);
                    if (isFirstRun) {
                        Intent intent = new Intent(AddWalletActivity.this, DashBoardActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        finish();
                    }
                });
            });
        });
    }

    // Hàm cập nhật trạng thái các nút chọn
    private void updateUIState() {
        // Disable nút Tiền mặt nếu đã có
        radioCash.setEnabled(!hasCashWallet);
        radioCash.setAlpha(hasCashWallet ? 0.5f : 1.0f);

        // Disable nút Chuyển khoản nếu đã có
        radioTransfer.setEnabled(!hasBankWallet);
        radioTransfer.setAlpha(hasBankWallet ? 0.5f : 1.0f);

        // Tự động chuyển vùng chọn nếu cái hiện tại bị disable
        if (hasCashWallet && radioCash.isChecked()) {
            radioTransfer.setChecked(true);
        }
        if (hasBankWallet && radioTransfer.isChecked()) {
            radioCash.setChecked(true);
        }

        // Nếu cả 2 đều có -> Chặn nút tạo luôn
        if (hasCashWallet && hasBankWallet) {
            btnCreate.setEnabled(false);
            btnCreate.setText("Đã đủ số lượng ví (Tối đa 2)");
            btnCreate.setBackgroundColor(getResources().getColor(R.color.neutral_400));
        }
    }
}