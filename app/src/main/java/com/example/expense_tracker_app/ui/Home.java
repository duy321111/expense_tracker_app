package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton; // SỬA: Thêm import cho ImageButton
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast; // SỬA: Thêm import cho Toast

import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.ui.Budget.BudgetDetailActivity;
import com.example.expense_tracker_app.ui.Loan.LoanTrackingActivity;
import com.example.expense_tracker_app.ui.Notification.NotificationActivity;
import com.example.expense_tracker_app.ui.stats.StatsActivity;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;

public class Home extends AppCompatActivity {

    // SỬA: Khai báo các nút nav
    private ImageButton btnNavHome, btnNavReport, btnNavBudget, btnNavProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Avatar -> UserInfor
        findViewById(R.id.imgAvatar).setOnClickListener(v ->
                startActivity(new Intent(Home.this, UserInfor.class)));

        MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);

        tb.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_reload) { recreate(); return true; }
            if (id == R.id.action_notification) {
                startActivity(new Intent(Home.this, NotificationActivity.class));
                return true;
            }
            return false;
        });


        // --- Biểu đồ thu chi ---
        View barIncome = findViewById(R.id.barIncome);
        View barSpending = findViewById(R.id.barSpending);

        float income = 17_000_000f;
        float spending = 7_945_000f;

        float maxValue = Math.max(income, spending);
        int chartMaxDp = 180;
        float density = getResources().getDisplayMetrics().density;

        int incomeHeight = Math.round(chartMaxDp * (income / maxValue) * density);
        int spendingHeight = Math.round(chartMaxDp * (spending / maxValue) * density);

        LinearLayout.LayoutParams lpIncome =
                (LinearLayout.LayoutParams) barIncome.getLayoutParams();
        lpIncome.height = incomeHeight;
        barIncome.setLayoutParams(lpIncome);

        LinearLayout.LayoutParams lpSpending =
                (LinearLayout.LayoutParams) barSpending.getLayoutParams();
        lpSpending.height = spendingHeight;
        barSpending.setLayoutParams(lpSpending);

        TextView y0 = findViewById(R.id.tvY0);
        TextView y25 = findViewById(R.id.tvY25);
        TextView y50 = findViewById(R.id.tvY50);
        TextView y75 = findViewById(R.id.tvY75);
        TextView y100 = findViewById(R.id.tvY100);

        float unit = 1_000_000f;
        y0.setText("0");
        y25.setText(Math.round(maxValue * 0.25f / unit) + "m");
        y50.setText(Math.round(maxValue * 0.50f / unit) + "m");
        y75.setText(Math.round(maxValue * 0.75f / unit) + "m");
        y100.setText(Math.round(maxValue / unit) + "m");

        // Xem tất cả
        findViewById(R.id.btnSeeAll).setOnClickListener(v ->
                startActivity(new Intent(Home.this, Transactions.class)));

        // --- Hạn mức chi tiêu ---
        int spent = 5_642_000;
        int limit = 12_000_000;

        ProgressBar pb = findViewById(R.id.pbBudget);
        pb.setMax(limit);
        pb.setProgress(spent);

        TextView tvRatio = findViewById(R.id.tvSpentRatio);
        tvRatio.setText(formatMoney(spent) + "/" + formatMoney(limit));

        Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);

        Calendar start = Calendar.getInstance();
        start.set(y, m, 1);

        Calendar end = Calendar.getInstance();
        end.set(y, m, end.getActualMaximum(Calendar.DAY_OF_MONTH));

        ((TextView) findViewById(R.id.tvMonthStart))
                .setText(two(start.get(Calendar.DAY_OF_MONTH)) + "/" + two(m + 1));
        ((TextView) findViewById(R.id.tvMonthEnd))
                .setText(two(end.get(Calendar.DAY_OF_MONTH)) + "/" + two(m + 1));


        // --- Hạn muwsc chi tiêu ---
        findViewById(R.id.tvDetailLimit).setOnClickListener(v ->
                startActivity(new Intent(Home.this, BudgetDetailActivity.class)));

        // --- Theo dõi vay nợ ---
        findViewById(R.id.tvDebtDetail).setOnClickListener(v ->
                startActivity(new Intent(Home.this, LoanTrackingActivity.class)));

        int paid = 4_500_000;
        int total = 12_000_000;
        String form = "Đi vay";
        String partner = "Home Credit";

        ProgressBar pbDebt = findViewById(R.id.pbDebt);
        pbDebt.setMax(total);
        pbDebt.setProgress(paid);

        ((TextView) findViewById(R.id.tvDebtRatio))
                .setText(formatMoney(paid) + "/" + formatMoney(total));
        ((TextView) findViewById(R.id.tvDebtMeta))
                .setText(form + " - " + partner);

        ((TextView) findViewById(R.id.tvDebtStart))
                .setText(two(start.get(Calendar.DAY_OF_MONTH)) + "/" + two(m + 1));
        ((TextView) findViewById(R.id.tvDebtEnd))
                .setText(two(end.get(Calendar.DAY_OF_MONTH)) + "/" + two(m + 1));

        // --- Spinner chọn ví ---
        Spinner spinnerWallet = findViewById(R.id.spinner_wallet);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.wallet_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWallet.setAdapter(adapter);

        spinnerWallet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWallet = parent.getItemAtPosition(position).toString();
                Toast.makeText(Home.this, "Đã chọn: " + selectedWallet, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        // --- (KẾT THÚC CODE CŨ CỦA BẠN) ---


        // --- SỬA: THÊM CODE MỚI CHO NAVIGATION BAR ---
        initBottomNavigation();
    }

    /**
     * SỬA: Thêm hàm mới để gán logic cho thanh Navigation
     */
    private void initBottomNavigation() {
        // 1. Ánh xạ các nút từ layout (đã được include)
        btnNavHome = findViewById(R.id.btn_nav_home);
        btnNavReport = findViewById(R.id.btn_nav_report);
        btnNavBudget = findViewById(R.id.btn_nav_budget);
        btnNavProfile = findViewById(R.id.btn_nav_profile);

        // 2. Gán hành động (Action)

        // Nút Home (Trang hiện tại)
        btnNavHome.setOnClickListener(v -> {
            Toast.makeText(Home.this, "Bạn đang ở Trang chủ", Toast.LENGTH_SHORT).show();
            // Không làm gì cả vì đang ở Home
        });

        // Nút Profile -> Mở ProfileActivity (đã tạo ở Bước 3)
        btnNavProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Nút Report (Thống kê)
        btnNavReport.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, StatsActivity.class);
            startActivity(intent);
        });

        // Nút Budget (Ngân sách)
        btnNavBudget.setOnClickListener(v -> {
            // TODO: Tạo BudgetActivity.class giống như cách bạn tạo ProfileActivity
            Toast.makeText(Home.this, "Mở trang Ngân sách", Toast.LENGTH_SHORT).show();
            // Ví dụ: startActivity(new Intent(Home.this, BudgetActivity.class));
        });
    }

    // --- (CÁC HÀM HELPER CŨ CỦA BẠN - GIỮ NGUYÊN) ---
    private String two(int n) { return (n < 10 ? "0" : "") + n; }

    private String formatMoney(int value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        return new DecimalFormat("#,###", symbols).format(value);
    }
}

