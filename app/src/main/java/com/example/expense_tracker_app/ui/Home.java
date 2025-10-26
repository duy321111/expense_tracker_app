package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.ui.Notification.NotificationActivity;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;

public class Home extends Fragment {

    private ImageButton btnNavHome, btnNavReport, btnNavBudget, btnNavProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout
        return inflater.inflate(R.layout.home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Toolbar ---
        MaterialToolbar tb = view.findViewById(R.id.toolbar);
        // Nếu muốn Toolbar fragment quản lý, dùng: ((AppCompatActivity)getActivity()).setSupportActionBar(tb);

        // --- Avatar ---
        view.findViewById(R.id.imgAvatar).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ProfileFragment.class)));

        // --- Reload ---
        view.findViewById(R.id.btnReload).setOnClickListener(v -> {
            if(getActivity() != null) getActivity().recreate();
        });

        // --- Notification ---
        view.findViewById(R.id.btnNotification).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), NotificationActivity.class)));

        // --- Biểu đồ thu chi ---
        View barIncome = view.findViewById(R.id.barIncome);
        View barSpending = view.findViewById(R.id.barSpending);

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

        TextView y0 = view.findViewById(R.id.tvY0);
        TextView y25 = view.findViewById(R.id.tvY25);
        TextView y50 = view.findViewById(R.id.tvY50);
        TextView y75 = view.findViewById(R.id.tvY75);
        TextView y100 = view.findViewById(R.id.tvY100);

        float unit = 1_000_000f;
        y0.setText("0");
        y25.setText(Math.round(maxValue * 0.25f / unit) + "m");
        y50.setText(Math.round(maxValue * 0.50f / unit) + "m");
        y75.setText(Math.round(maxValue * 0.75f / unit) + "m");
        y100.setText(Math.round(maxValue / unit) + "m");

        // --- Xem tất cả ---
        view.findViewById(R.id.tvSeeAll).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), Transactions.class)));

        // --- Hạn mức chi tiêu ---
        int spent = 5_642_000;
        int limit = 12_000_000;

        ProgressBar pb = view.findViewById(R.id.pbBudget);
        pb.setMax(limit);
        pb.setProgress(spent);

        TextView tvRatio = view.findViewById(R.id.tvSpentRatio);
        tvRatio.setText(formatMoney(spent) + "/" + formatMoney(limit));

        Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);

        Calendar start = Calendar.getInstance();
        start.set(y, m, 1);
        Calendar end = Calendar.getInstance();
        end.set(y, m, end.getActualMaximum(Calendar.DAY_OF_MONTH));

        ((TextView) view.findViewById(R.id.tvMonthStart))
                .setText(two(start.get(Calendar.DAY_OF_MONTH)) + "/" + two(m + 1));
        ((TextView) view.findViewById(R.id.tvMonthEnd))
                .setText(two(end.get(Calendar.DAY_OF_MONTH)) + "/" + two(m + 1));

        view.findViewById(R.id.tvDetailLimit).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), SpendingLimit.class)));

        // --- Theo dõi vay nợ ---
        view.findViewById(R.id.tvDebtDetail).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), DebtTracking.class)));

        int paid = 4_500_000;
        int total = 12_000_000;
        String form = "Đi vay";
        String partner = "Home Credit";

        ProgressBar pbDebt = view.findViewById(R.id.pbDebt);
        pbDebt.setMax(total);
        pbDebt.setProgress(paid);

        ((TextView) view.findViewById(R.id.tvDebtRatio))
                .setText(formatMoney(paid) + "/" + formatMoney(total));
        ((TextView) view.findViewById(R.id.tvDebtMeta))
                .setText(form + " - " + partner);

        ((TextView) view.findViewById(R.id.tvDebtStart))
                .setText(two(start.get(Calendar.DAY_OF_MONTH)) + "/" + two(m + 1));
        ((TextView) view.findViewById(R.id.tvDebtEnd))
                .setText(two(end.get(Calendar.DAY_OF_MONTH)) + "/" + two(m + 1));

        // --- Spinner chọn ví ---
        Spinner spinnerWallet = view.findViewById(R.id.spinner_wallet);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.wallet_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWallet.setAdapter(adapter);

        spinnerWallet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                String selectedWallet = parent.getItemAtPosition(position).toString();
                Toast.makeText(getActivity(), "Đã chọn: " + selectedWallet, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // --- NAVIGATION BAR (nếu muốn logic ở fragment) ---
        // Có thể không cần, vì DashboardActivity quản lý
    }

    // --- HELPER ---
    private String two(int n) { return (n < 10 ? "0" : "") + n; }

    private String formatMoney(int value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        return new DecimalFormat("#,###", symbols).format(value);
    }
}
