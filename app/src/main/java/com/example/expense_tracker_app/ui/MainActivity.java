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

        // Mặc định mở Thống kê
        replace(new StatsFragment());
        b.bottomNav.setSelectedItemId(R.id.nav_reports);

        b.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // replace(new HomeFragment()); // nếu chưa có, tạm dùng Stats
                replace(new StatsFragment());
                return true;
            } else if (id == R.id.nav_reports) {
                replace(new StatsFragment());
                return true;
            } else if (id == R.id.nav_budget) {
                // replace(new BudgetFragment());
                replace(new StatsFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                // replace(new ProfileFragment());
                replace(new StatsFragment());
                return true;
            } else if (id == R.id.nav_add_placeholder) {
                return false; // slot cho FAB
            }
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
