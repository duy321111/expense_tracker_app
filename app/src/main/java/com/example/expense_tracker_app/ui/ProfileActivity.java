package com.example.expense_tracker_app.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expense_tracker_app.R;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chỉ cần gắn layout. NavHost sẽ tự động xử lý Fragment.
        setContentView(R.layout.activity_profile);
    }
}