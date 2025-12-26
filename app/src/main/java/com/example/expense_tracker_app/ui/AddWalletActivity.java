package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.database.AppDatabase;
import com.example.expense_tracker_app.data.model.Wallet;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.concurrent.Executors;

public class AddWalletActivity extends AppCompatActivity {

    private int getCurrentUserId() {
        // Ưu tiên: nhận từ Intent nếu bạn có truyền
        int fromIntent = getIntent().getIntExtra("USER_ID", -1);
        if (fromIntent != -1) return fromIntent;

        // Fallback: lấy từ SharedPreferences (bạn cần lưu userId lúc login)
        SharedPreferences sp = getSharedPreferences("session", MODE_PRIVATE);
        return sp.getInt("user_id", -1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wallet);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_add_wallet);
        toolbar.setNavigationOnClickListener(v -> finish());

        RadioGroup radioGroup = findViewById(R.id.radioGroupWalletType);
        RadioButton radioCash = findViewById(R.id.radio_cash);

        EditText etInitialBalance = findViewById(R.id.et_initial_balance);
        Button btnCreateWallet = findViewById(R.id.btn_create_wallet);

        btnCreateWallet.setOnClickListener(v -> {
            int userId = getCurrentUserId();
            if (userId == -1) {
                Toast.makeText(this, "Chưa xác định người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
                return;
            }

            String initialBalanceStr = etInitialBalance.getText().toString().trim();

            String walletName;
            String iconName;

            if (radioCash.isChecked()) {
                walletName = "Tiền mặt";
                iconName = "ic_wallet";
            } else {
                walletName = "Chuyển khoản";
                iconName = "ic_settings";
            }

            double initialBalance = 0.0;
            if (!initialBalanceStr.isEmpty()) {
                try {
                    initialBalance = Double.parseDouble(initialBalanceStr);
                } catch (NumberFormatException e) {
                    etInitialBalance.setError("Số dư không hợp lệ!");
                    return;
                }
            }

            // QUAN TRỌNG: tạo Wallet có userId
            Wallet newWallet = new Wallet(userId, walletName, initialBalance, iconName);

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
