package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker_app.R;
import android.widget.Button;
import android.widget.TextView;
public class OTPVerification extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.otp_verification);

        TextView tvLogin = findViewById(R.id.tvLoginRedirect);
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(OTPVerification.this, Login.class);
            startActivity(intent);
        });

        Button btnVerifyOTP = findViewById(R.id.btnVerifyOTP);
        btnVerifyOTP.setOnClickListener(v -> {
            Intent intent = new Intent(OTPVerification.this, ResetPassword.class);
            startActivity(intent);
        });
    }


}