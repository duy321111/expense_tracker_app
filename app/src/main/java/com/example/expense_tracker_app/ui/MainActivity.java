package com.example.expense_tracker_app.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding b;

    @Override protected void onCreate(Bundle s){
        super.onCreate(s);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // Mặc định mở màn Thêm giao dịch
        replace(new AddTransactionFragment());

        b.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // TODO: thay bằng HomeFragment khi có
                replace(new AddTransactionFragment());
                return true;
            }

            if (id == R.id.nav_budget) {
                // TODO: thay bằng BudgetFragment khi có
                replace(new AddTransactionFragment());
                return true;
            }

            if (id == R.id.nav_profile) {
                // TODO: thay bằng ProfileFragment khi có
                replace(new AddTransactionFragment());
                return true;
            }

            // slot cho FAB (không xử lý chọn)
            if (id == R.id.nav_add_placeholder) return false;

            return false;
        });

        b.fabAdd.setOnClickListener(v ->
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new AddTransactionFragment())
                        .addToBackStack(null)
                        .commit()
        );
    }

    private void replace(Fragment f){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, f)
                .commit();
    }
}
