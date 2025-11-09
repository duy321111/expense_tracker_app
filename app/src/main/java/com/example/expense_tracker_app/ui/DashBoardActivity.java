package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.expense_tracker_app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.expense_tracker_app.ui.stats.StatsActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class DashBoardActivity extends AppCompatActivity {

    private LinearLayout btnNavHome, btnNavStats, btnNavBudget, btnNavProfile;
    private FloatingActionButton fab;

    // Tham chiếu icon + text
    private ImageView iconHome, iconStats, iconBudget, iconProfile;
    private TextView textHome, textStats, textBudget, textProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initBottomNavigation();
        initFAB();

        // Mặc định chọn Home
        setSelectedNav(btnNavHome);
        switchFragment(new Home());
    }

    private void initBottomNavigation() {
        btnNavHome = findViewById(R.id.btn_nav_home);
        btnNavBudget = findViewById(R.id.btn_nav_budget);
        btnNavProfile = findViewById(R.id.btn_nav_profile);
        btnNavStats = findViewById(R.id.btn_nav_stats);

        iconHome = findViewById(R.id.icon_home);
        iconStats = findViewById(R.id.icon_stats);
        iconBudget = findViewById(R.id.icon_budget);
        iconProfile = findViewById(R.id.icon_profile);

        textHome = findViewById(R.id.text_home);
        textStats = findViewById(R.id.text_stats);
        textBudget = findViewById(R.id.text_budget);
        textProfile = findViewById(R.id.text_profile);

        btnNavHome.setOnClickListener(v -> {
            setSelectedNav(btnNavHome);
            switchFragment(new Home());
        });

        btnNavBudget.setOnClickListener(v -> {
            setSelectedNav(btnNavBudget);
            switchFragment(new BudgetHomePage());
        });

        btnNavProfile.setOnClickListener(v -> {
            setSelectedNav(btnNavProfile);
            switchFragment(new ProfileFragment());
        });

        btnNavStats.setOnClickListener(v -> {
            setSelectedNav(btnNavStats);
            switchFragment(new StatsActivity());

        });
    }

    private void setSelectedNav(LinearLayout selectedBtn) {
        // Reset trạng thái
        btnNavHome.setSelected(false);
        btnNavStats.setSelected(false);
        btnNavBudget.setSelected(false);
        btnNavProfile.setSelected(false);

        // Đặt selected = true cho nút được chọn
        selectedBtn.setSelected(true);
    }

    private void initFAB() {
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new AddTransactionFragment())
                    .addToBackStack(null)
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
