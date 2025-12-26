package com.example.expense_tracker_app.ui.Auth;

import android.content.Intent;
import android.content.SharedPreferences;
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
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // ✅ Auto-login dựa trên session user_id (không dùng repository.getLoggedInUser nữa)
        SharedPreferences sp = getSharedPreferences("session", MODE_PRIVATE);
        int sessionUserId = sp.getInt("user_id", -1);
        if (sessionUserId > 0) {
            startActivity(new Intent(Login.this, DashBoardActivity.class));
            finish();
            return;
        }

        // ✅ Khi ViewModel trả về user đang login -> lưu vào session
        loginViewModel.loggedUser.observe(this, user -> {
            if (user == null) return;

            sp.edit()
                    .putInt("user_id", user.id)
                    .apply();
        });

        // Sự kiện Login
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            loginViewModel.login(email, password);
        });

        // Điều hướng theo kết quả
        loginViewModel.loginResult.observe(this, status -> {
            if (status == null) return;

            if (status == 1) {
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Login.this, DashBoardActivity.class));
                finish();
            } else if (status == 2) {
                Toast.makeText(this, "Chào mừng! Hãy tạo ví đầu tiên của bạn", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Login.this, AddWalletActivity.class);
                intent.putExtra("IS_FIRST_RUN", true);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Email hoặc mật khẩu sai", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegister.setOnClickListener(v -> startActivity(new Intent(Login.this, Register.class)));

        tvForgotPassword.setOnClickListener(v -> {
            // TODO: handle forgot password
        });
    }
}
