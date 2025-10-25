package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.expense_tracker_app.R;
import com.google.android.material.appbar.MaterialToolbar;

public class AddWalletActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wallet);

        // Khởi tạo Spinner cho tiền tệ
        Spinner spinnerCurrency = findViewById(R.id.spinner_currency);

        // Sử dụng layout tùy chỉnh để đồng bộ style Body2
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.currencies,
                R.layout.spinner_item // layout hiển thị chính
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item); // layout khi xổ ra
        spinnerCurrency.setAdapter(adapter);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_add_wallet);
        toolbar.setNavigationOnClickListener(v -> finish());

        //  Khởi tạo các View khác
        Button btnCreateWallet = findViewById(R.id.btn_create_wallet);
        EditText etWalletName = findViewById(R.id.et_wallet_name);
        EditText etInitialBalance = findViewById(R.id.et_initial_balance);


        // Xử lý khi nhấn nút "Tạo ví"
        btnCreateWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String walletName = etWalletName.getText().toString().trim();
                String initialBalanceStr = etInitialBalance.getText().toString().trim();
                String selectedCurrency = spinnerCurrency.getSelectedItem().toString();

                if (walletName.isEmpty()) {
                    etWalletName.setError("Vui lòng nhập tên ví!");
                    return;
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

                // TODO: Lưu thông tin ví (walletName, initialBalance, selectedCurrency)

                TextView btn_create_wallet = findViewById(R.id.btn_create_wallet);

                    Intent intent = new Intent(AddWalletActivity.this, Home.class);
                    startActivity(intent);

            }
        });
    }
}
