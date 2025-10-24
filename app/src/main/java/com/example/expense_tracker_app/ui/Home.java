package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker_app.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // attach Toolbar SAU setContentView
        MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Avatar -> UserInfor
        findViewById(R.id.imgAvatar).setOnClickListener(v ->
                startActivity(new Intent(Home.this, UserInfor.class)));

        // Reload
        findViewById(R.id.btnReload).setOnClickListener(v -> recreate());

        // Notification
        findViewById(R.id.btnNotification).setOnClickListener(v ->
                startActivity(new Intent(Home.this, Notifications.class)));

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
        findViewById(R.id.tvSeeAll).setOnClickListener(v ->
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

        findViewById(R.id.tvDetailLimit).setOnClickListener(v ->
                startActivity(new Intent(Home.this, SpendingLimit.class)));

        // --- Theo dõi vay nợ ---
        findViewById(R.id.tvDebtDetail).setOnClickListener(v ->
                startActivity(new Intent(Home.this, DebtTracking.class)));

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
    }

    // helpers
    private String two(int n) { return (n < 10 ? "0" : "") + n; }

    private String formatMoney(int value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        return new DecimalFormat("#,###", symbols).format(value);
    }
}
