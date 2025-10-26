package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import com.example.expense_tracker_app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class DashBoardActivity extends AppCompatActivity {

    private ImageButton btnNavHome, btnNavReport, btnNavBudget, btnNavProfile;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initBottomNavigation();
        initFAB();

        // Mặc định load HomeFragment
        switchFragment(new Home());
    }

    private void initBottomNavigation() {
        btnNavHome = findViewById(R.id.btn_nav_home);
        btnNavReport = findViewById(R.id.btn_nav_report);
        btnNavBudget = findViewById(R.id.btn_nav_budget);
        btnNavProfile = findViewById(R.id.btn_nav_profile);

        btnNavHome.setOnClickListener(v -> switchFragment(new Home()));
        //btnNavReport.setOnClickListener(v -> switchFragment(new ReportFragment()));
        btnNavBudget.setOnClickListener(v -> switchFragment(new BudgetHomePage()));
        btnNavProfile.setOnClickListener(v -> switchFragment(new ProfileFragment()));
    }

    private void initFAB() {
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            // Mở màn hình thêm giao dịch
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new AddTransactionFragment())
                    .addToBackStack(null) // để nhấn back trở lại dashboard
                    .commit();
        });
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
