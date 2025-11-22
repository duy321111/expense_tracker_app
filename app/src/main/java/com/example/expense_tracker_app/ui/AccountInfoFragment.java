package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.ui.Auth.ResetPassword;

public class AccountInfoFragment extends AppCompatActivity {

    private Button btnChangePassword;
    private Button btnSave;
    private ImageView btnBack;

    private EditText etName, etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_account_info); // đổi layout nếu cần

        // 1. Tìm các view
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnSave = findViewById(R.id.btn_save);
        btnBack = findViewById(R.id.btn_back);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        // 2. Xử lý click "Đổi mật khẩu"
        btnChangePassword.setOnClickListener(v -> {
            // Mở Activity đổi mật khẩu
            startActivity(new Intent(AccountInfoFragment.this, ResetPassword.class));
        });

        // 3. Xử lý click "Lưu thông tin"
        btnSave.setOnClickListener(v -> {
            // TODO: Lưu thông tin từ EditText
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Hiển thị toast tạm thời
            Toast.makeText(AccountInfoFragment.this, "Đã lưu thông tin", Toast.LENGTH_SHORT).show();
        });

        // 4. Click quay lại
        btnBack.setOnClickListener(v -> finish());
    }
}
