package com.example.expense_tracker_app.ui;

import android.content.Context;
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
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.database.AppDatabase;
import com.example.expense_tracker_app.data.model.Wallet;
import com.example.expense_tracker_app.ui.Loan.LoanTrackingActivity;
import com.example.expense_tracker_app.ui.Notification.NotificationActivity;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;


public class Home extends Fragment {

    private ImageButton btnNavHome, btnNavReport, btnNavBudget, btnNavProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //seedWalletForTest();

        // --- Toolbar ---
        MaterialToolbar tb = view.findViewById(R.id.toolbar);

        // --- Avatar ---
        view.findViewById(R.id.imgAvatar).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ProfileFragment.class)));

        view.findViewById(R.id.btnReload).setOnClickListener(v -> requireActivity().recreate());
        view.findViewById(R.id.btnNotification).setOnClickListener(
                v -> startActivity(new Intent(requireContext(), NotificationActivity.class))
        );


        // --- Wallet UI ---
        Spinner spinnerWallet = view.findViewById(R.id.spinner_wallet);
        TextView tvWalletOwner = view.findViewById(R.id.tv_wallet_owner);
        loadLoggedInUser(tvWalletOwner);

        TextView tvWalletBalance = view.findViewById(R.id.tv_wallet_balance);
        TextView tvTotalBalance  = view.findViewById(R.id.tv_total_balance);

        // spinner đổi ví -> set tvWalletBalance
        loadWalletFromDb(spinnerWallet, tvWalletBalance, tvTotalBalance);

        // tổng số dư
        //loadTotalBalance(tvTotalBalance);


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
                startActivity(new Intent(getActivity(), BudgetDetailActivity.class)));

        // --- Theo dõi vay nợ ---
        view.findViewById(R.id.tvDebtDetail).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), LoanTrackingActivity.class)));

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

    }

    private void loadWalletFromDb(Spinner spinnerWallet,
                                  TextView tvWalletBalance,
                                  TextView tvTotalBalance) {
        int userId = requireActivity()
                .getSharedPreferences("session", Context.MODE_PRIVATE)
                .getInt("user_id", -1);

        if (userId == -1) {
            tvWalletBalance.setText("0 VND");
            tvTotalBalance.setText("0 VND");
            spinnerWallet.setAdapter(null);
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<Wallet> wallets = db.walletDao().getWalletsByUser(userId);

            requireActivity().runOnUiThread(() -> {
                if (wallets == null || wallets.isEmpty()) {
                    tvWalletBalance.setText("0 VND");
                    tvTotalBalance.setText("0 VND");
                    spinnerWallet.setAdapter(null);
                    return;
                }

                ArrayAdapter<Wallet> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        wallets
                ) {
                    @NonNull
                    @Override
                    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        ((TextView) v.findViewById(android.R.id.text1)).setText(wallets.get(position).name);
                        return v;
                    }

                    @NonNull
                    @Override
                    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                        View v = super.getDropDownView(position, convertView, parent);
                        ((TextView) v.findViewById(android.R.id.text1)).setText(wallets.get(position).name);
                        return v;
                    }
                };

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerWallet.setAdapter(adapter);

                // ví mặc định
                Wallet first = wallets.get(0);
                tvWalletBalance.setText(formatVnd(first.balance));

                // load tổng ngay sau khi có data
                loadTotalBalance(tvTotalBalance);

                spinnerWallet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Wallet selected = (Wallet) parent.getItemAtPosition(position);
                        tvWalletBalance.setText(formatVnd(selected.balance));

                        // tổng không đổi khi đổi ví, nhưng gọi lại cũng không sao
                        loadTotalBalance(tvTotalBalance);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) { }
                });
            });
        });
    }



    // --- HELPER ---
    private String two(int n) { return (n < 10 ? "0" : "") + n; }

    private String formatMoney(int value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        return new DecimalFormat("#,###", symbols).format(value);
    }

    private String formatVnd(double amount) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(amount) + " VND";
    }





    private void loadLoggedInUser(TextView tvWalletOwner) {
        int userId = requireActivity()
                .getSharedPreferences("session", Context.MODE_PRIVATE)
                .getInt("user_id", -1);

        if (userId <= 0) { // 0 hoặc -1
            tvWalletOwner.setText("Chưa có user đăng nhập");
            return;
        }

        AppDatabase db = AppDatabase.getInstance(requireContext());
        db.userDao().getUserById(userId).observe(getViewLifecycleOwner(), user -> {
            if (user != null) tvWalletOwner.setText(user.fullName);
        });
    }

    private void loadTotalBalance(TextView tvTotalBalance) {
        int userId = requireActivity()
                .getSharedPreferences("session", Context.MODE_PRIVATE)
                .getInt("user_id", -1);

        if (userId == -1) {
            tvTotalBalance.setText("0 VND");
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            Double total = db.walletDao().getTotalBalanceByUser(userId);

            final double finalTotal = (total == null) ? 0.0 : total;

            requireActivity().runOnUiThread(() -> {
                tvTotalBalance.setText(formatVnd(finalTotal));
            });
        });
    }





    private void seedWalletForTest() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());

            // chỉ insert nếu chưa có ví
            if (db.walletDao().getWalletCount() == 0) {

                int userId = 1; // test cứng

                db.walletDao().insertWallet(
                        new Wallet(userId, "Tiền mặt", 2_000_000, "ic_wallet")
                );

                db.walletDao().insertWallet(
                        new Wallet(userId, "Chuyển khoản", 5_000_000, "ic_settings")
                );
            }
        });
    }





}
