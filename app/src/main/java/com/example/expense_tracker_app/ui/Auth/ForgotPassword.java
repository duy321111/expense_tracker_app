package com.example.expense_tracker_app.ui.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker_app.R;

public class ForgotPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.forgot_password);

        TextView tvLogin = findViewById(R.id.tvLoginRedirect);
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPassword.this, Login.class);
            startActivity(intent);
        });

        Button btnSendReset = findViewById(R.id.btnSendReset);
        btnSendReset.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPassword.this, OTPVerification.class);
            startActivity(intent);
        });
    }
}