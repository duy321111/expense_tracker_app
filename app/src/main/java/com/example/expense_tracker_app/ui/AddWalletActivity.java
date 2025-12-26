package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.database.AppDatabase;
import com.example.expense_tracker_app.data.model.User;
import com.example.expense_tracker_app.data.model.Wallet;
import com.example.expense_tracker_app.data.repository.UserRepository;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.concurrent.Executors;

public class AddWalletActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wallet);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_add_wallet);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Views
        RadioGroup radioGroup = findViewById(R.id.radioGroupWalletType);
        RadioButton radioCash = findViewById(R.id.radio_cash);
        // RadioButton radioTransfer = findViewById(R.id.radio_transfer);

        EditText etInitialBalance = findViewById(R.id.et_initial_balance);
        Button btnCreateWallet = findViewById(R.id.btn_create_wallet);

        btnCreateWallet.setOnClickListener(v -> {
            String initialBalanceStr = etInitialBalance.getText().toString().trim();

            // 1. Xác định tên ví và Icon dựa trên lựa chọn
            String walletName;
            String iconName;

            if (radioCash.isChecked()) {
                walletName = "Tiền mặt";
                iconName = "ic_wallet"; // Đảm bảo icon này có trong drawable
            } else {
                walletName = "Chuyển khoản";
                iconName = "ic_settings"; // Hoặc icon ngân hàng nếu bạn có
            }

            // 2. Parse số dư
            double initialBalance = 0.0;
            if (!initialBalanceStr.isEmpty()) {
                try {
                    initialBalance = Double.parseDouble(initialBalanceStr);
                } catch (NumberFormatException e) {
                    etInitialBalance.setError("Số dư không hợp lệ!");
                    return;
                }
            }

            // 3. Lấy user hiện tại
            UserRepository userRepository = new UserRepository(this);
            User currentUser = userRepository.getLoggedInUser();
            
            if (currentUser == null) {
                Toast.makeText(AddWalletActivity.this, "Lỗi: Không tìm thấy user đang đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 4. Lưu vào Database cùng với userId
            Wallet newWallet = new Wallet(walletName, initialBalance, iconName, currentUser.id);

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                db.walletDao().insertWallet(newWallet);

                runOnUiThread(() -> {
                    Toast.makeText(AddWalletActivity.this, "Đã thêm ví: " + walletName, Toast.LENGTH_SHORT).show();

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
}