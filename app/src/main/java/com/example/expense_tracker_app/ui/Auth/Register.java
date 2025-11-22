package com.example.expense_tracker_app.ui.Auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.User;
import com.example.expense_tracker_app.data.repository.UserRepository;

public class Register extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnCreateAccount;
    private TextView tvLoginRedirect;
    private UserRepository repository;

    private EditText edtFullName, edtConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // --- Ánh xạ view ---
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvLoginRedirect = findViewById(R.id.tvLoginRedirect);
        edtFullName = findViewById(R.id.edtFullName);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        repository = new UserRepository(this);

        // --- Đăng ký ---
        btnCreateAccount.setOnClickListener(v -> {
            String fullName = edtFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu không trùng khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = repository.registerUser(new User(fullName, email, password));

            if (success) {
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                repository.checkLogin(email, password);
                finish();
            } else {
                Toast.makeText(this, "Đăng ký thất bại hoặc email đã tồn tại", Toast.LENGTH_SHORT).show();
            }
        });

        // --- Chuyển sang Login ---
        tvLoginRedirect.setOnClickListener(v -> finish());
    }
}
