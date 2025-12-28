package com.example.expense_tracker_app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Wallet;
import com.example.expense_tracker_app.data.repository.TransactionRepository;
import com.example.expense_tracker_app.data.repository.UserRepository;
import com.example.expense_tracker_app.ui.Loan.LoanTrackingActivity;
import com.example.expense_tracker_app.ui.Notification.NotificationActivity;
import com.example.expense_tracker_app.ui.adapter.TransactionAdapter;
import com.example.expense_tracker_app.viewmodel.ProfileViewModel;
import com.example.expense_tracker_app.viewmodel.WalletViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;              // ✅ FIX: import LocalDate
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Home extends Fragment {

    private ProfileViewModel profileViewModel;
    private WalletViewModel walletViewModel;

    private int userId;

    // user
    private TextView tvUserName;
    private TextView tvWalletOwner;

    // wallet
    private TextView tvWalletBalance;
    private TextView tvTotalWalletBalance;
    private Spinner spinnerWallet;
    private final List<Wallet> walletList = new ArrayList<>();
    private ArrayAdapter<String> walletNameAdapter;

    // debt card (loan tracking summary)
    private TextView tvDebtRatio;
    private ProgressBar pbDebt;
    private TextView tvDebtMeta;

    // ✅ FIX: repository dùng cho debt summary
    private TransactionRepository transactionRepository;

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

        // --- ViewModels ---
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);

        // ✅ FIX: init TransactionRepository 1 lần
        transactionRepository = new TransactionRepository(requireActivity().getApplication());

        // --- userId (1 nguồn) ---
        UserRepository userRepository = new UserRepository(requireActivity());
        userId = (userRepository.getLoggedInUser() != null) ? userRepository.getLoggedInUser().id : -1;

        // --- Init views ---
        MaterialToolbar tb = view.findViewById(R.id.toolbar);

        tvUserName = view.findViewById(R.id.tvUserName);
        tvWalletOwner = view.findViewById(R.id.tv_wallet_owner);

        tvWalletBalance = view.findViewById(R.id.tv_wallet_balance);
        spinnerWallet = view.findViewById(R.id.spinner_wallet);
        tvTotalWalletBalance = view.findViewById(R.id.tv_total_wallet_balance);

        tvDebtRatio = view.findViewById(R.id.tvDebtRatio);
        pbDebt = view.findViewById(R.id.pbDebt);
        tvDebtMeta = view.findViewById(R.id.tvDebtMeta);
        setupDebtCard();


        // --- user name ---
        observeUserName();

        // --- wallet ---
        setupWalletSpinner();
        observeWalletsUsingExistingViewModel();

        // ✅ FIX: gọi setupDebtCard để load DB lên progress
        setupDebtCard();

        // --- events ---
        view.findViewById(R.id.btnReload).setOnClickListener(v -> requireActivity().recreate());
        view.findViewById(R.id.btnNotification).setOnClickListener(
                v -> startActivity(new Intent(requireContext(), NotificationActivity.class))
        );

        view.findViewById(R.id.tvDebtDetail).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), LoanTrackingActivity.class))
        );

        // --- chart (mock) ---
        View barIncome = view.findViewById(R.id.barIncome);
        View barSpending = view.findViewById(R.id.barSpending);

        float income = 17_000_000f;
        float spending = 7_945_000f;
        float maxValue = Math.max(income, spending);
        int chartMaxDp = 180;
        float density = getResources().getDisplayMetrics().density;

        int incomeHeight = Math.round(chartMaxDp * (income / maxValue) * density);
        int spendingHeight = Math.round(chartMaxDp * (spending / maxValue) * density);

        LinearLayout.LayoutParams lpIncome = (LinearLayout.LayoutParams) barIncome.getLayoutParams();
        lpIncome.height = incomeHeight;
        barIncome.setLayoutParams(lpIncome);

        LinearLayout.LayoutParams lpSpending = (LinearLayout.LayoutParams) barSpending.getLayoutParams();
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

        // --- month range text ---
        Calendar c = Calendar.getInstance();
        int yy = c.get(Calendar.YEAR);
        int mm = c.get(Calendar.MONTH);

        Calendar start = Calendar.getInstance();
        start.set(yy, mm, 1);
        Calendar end = Calendar.getInstance();
        end.set(yy, mm, end.getActualMaximum(Calendar.DAY_OF_MONTH));

        // --- recent transactions ---
        view.findViewById(R.id.tvSeeAll).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), Transactions.class))
        );

        RecyclerView rvRecent = view.findViewById(R.id.rv_recent_transactions);
        TextView tvEmpty = view.findViewById(R.id.tv_empty_recent);

        rvRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        TransactionAdapter recentAdapter = new TransactionAdapter(requireContext());
        rvRecent.setAdapter(recentAdapter);

        loadRecentTransactions(recentAdapter, tvEmpty);
    }

    // ===================== WALLET =====================

    private void setupWalletSpinner() {
        walletNameAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        walletNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWallet.setAdapter(walletNameAdapter);

        spinnerWallet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                if (position >= 0 && position < walletList.size()) {
                    Wallet w = walletList.get(position);
                    tvWalletBalance.setText(formatMoneyDouble(w.balance) + " đ");
                } else {
                    tvWalletBalance.setText("0 đ");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void observeWalletsUsingExistingViewModel() {
        walletViewModel.wallets.observe(getViewLifecycleOwner(), wallets -> {
            walletList.clear();
            walletNameAdapter.clear();

            if (wallets != null && !wallets.isEmpty()) {
                walletList.addAll(wallets);
                for (Wallet w : wallets) walletNameAdapter.add(w.name);
                tvWalletBalance.setText(formatMoneyDouble(walletList.get(0).balance) + " đ");
            } else {
                walletNameAdapter.add("Chưa có ví");
                tvWalletBalance.setText("0 đ");
            }

            walletNameAdapter.notifyDataSetChanged();
        });

        if (tvTotalWalletBalance != null) {
            walletViewModel.totalBalance.observe(getViewLifecycleOwner(), total -> {
                double t = (total != null) ? total : 0.0;
                tvTotalWalletBalance.setText(formatMoneyDouble(t) + " đ");
            });
        }
    }

    // ===================== USER =====================

    private void observeUserName() {
        if (userId == -1) {
            tvUserName.setText("Người dùng");
            tvWalletOwner.setText("Người dùng");
            return;
        }

        profileViewModel.getUser(userId).observe(getViewLifecycleOwner(), user -> {
            String name = "Người dùng";
            if (user != null && user.fullName != null && !user.fullName.isEmpty()) {
                name = user.fullName;
            }
            tvUserName.setText(name);
            tvWalletOwner.setText(name);
        });
    }

    // ===================== RECENT TX =====================

    private void loadRecentTransactions(TransactionAdapter adapter, TextView tvEmpty) {
        int uid = requireActivity()
                .getSharedPreferences("session", Context.MODE_PRIVATE)
                .getInt("user_id", -1);

        if (uid <= 0) {
            adapter.setData(java.util.Collections.emptyList());
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        TransactionRepository repo = new TransactionRepository(requireActivity().getApplication());
        LocalDate now = LocalDate.now();

        repo.getTransactionsByMonth(uid, now).observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                adapter.setData(java.util.Collections.emptyList());
                tvEmpty.setVisibility(View.VISIBLE);
            } else {
                int end = Math.min(3, list.size());
                adapter.setData(list.subList(0, end));
                tvEmpty.setVisibility(View.GONE);
            }
        });
    }

    // ===================== LOAN TRACKING SUMMARY (CARD HOME) =====================

    private void setupDebtCard() {
        if (tvDebtRatio == null || pbDebt == null || tvDebtMeta == null) return;

        if (userId <= 0) {
            renderDebtEmpty();
            return;
        }

        LocalDate now = LocalDate.now();

        // YÊU CẦU: TransactionRepository phải có getDebtSummaryByMonth(userId, now)
        transactionRepository.getDebtSummaryByMonth(userId, now)
                .observe(getViewLifecycleOwner(), summary -> {
                    if (summary == null) {
                        renderDebtEmpty();
                        return;
                    }

                    long total = summary.totalBorrow;
                    long paid = summary.totalPaid;

                    if (total <= 0) {
                        renderDebtEmpty();
                        return;
                    }

                    tvDebtRatio.setText(formatMoneyLong(paid) + " / " + formatMoneyLong(total));

                    int safeMax = (int) Math.min(Integer.MAX_VALUE, Math.max(1, total));
                    int safeProg = (int) Math.min(safeMax, Math.min(Integer.MAX_VALUE, Math.max(0, paid)));

                    pbDebt.setMax(safeMax);
                    pbDebt.setProgress(safeProg);

                    tvDebtMeta.setText("Đã trả " + formatMoneyLong(paid) + " trên tổng nợ " + formatMoneyLong(total));
                });
    }

    private void renderDebtEmpty() {
        tvDebtRatio.setText("0 / 0");
        pbDebt.setMax(100);
        pbDebt.setProgress(0);
        tvDebtMeta.setText("Chưa phát sinh khoản vay trong tháng");
    }

    // ===================== HELPERS =====================

    private String formatMoneyLong(long value) {  // ✅ FIX: thêm hàm này
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        return new DecimalFormat("#,###", symbols).format(value);
    }

    private String formatMoneyDouble(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        return new DecimalFormat("#,###", symbols).format(value);
    }
}
