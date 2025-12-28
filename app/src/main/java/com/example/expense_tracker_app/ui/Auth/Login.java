package com.example.expense_tracker_app.ui.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.User;
import com.example.expense_tracker_app.ui.AddWalletActivity;
import com.example.expense_tracker_app.ui.DashBoardActivity;
import com.example.expense_tracker_app.viewmodel.LoginViewModel;

public class Login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);


        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Auto-login (Giữ nguyên hoặc sửa nếu muốn check ví cả lúc auto-login)
        User loggedUser = loginViewModel.repository.getLoggedInUser();
        if (loggedUser != null) {
            startActivity(new Intent(Login.this, DashBoardActivity.class));
            finish();
        }

        // Sự kiện Login
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            loginViewModel.login(email, password);
        });

        // Quan sát kết quả Login (Code mới)
        loginViewModel.loginResult.observe(this, status -> {
            if (status == 1) {
                // Đã có ví -> Vào trang chủ
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Login.this, DashBoardActivity.class));
                finish();
            } else if (status == 2) {
                // Chưa có ví -> Chuyển sang tạo ví đầu tiên
                Toast.makeText(this, "Chào mừng! Hãy tạo ví đầu tiên của bạn", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Login.this, AddWalletActivity.class);
                // Gửi cờ đánh dấu đây là lần đầu chạy
                intent.putExtra("IS_FIRST_RUN", true);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Email hoặc mật khẩu sai", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegister.setOnClickListener(v -> startActivity(new Intent(Login.this, Register.class)));


    }
}