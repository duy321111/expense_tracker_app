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

        // Auto-login
        User loggedUser = loginViewModel.repository.getLoggedInUser();
        if (loggedUser != null) {
            startActivity(new Intent(Login.this, AddWalletActivity.class));
            finish();
        }

        //Login
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            loginViewModel.login(email, password);
        });

        loginViewModel.loginResult.observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Login thành công", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Login.this, AddWalletActivity.class));
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
