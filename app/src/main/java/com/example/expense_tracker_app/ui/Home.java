package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.TxType;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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

    // recent tx
    private TransactionRepository transactionRepository;

    // debt cards (RecyclerView dùng item_budget, tối đa 2 thẻ)
    private RecyclerView rvDebtCards;
    private DebtCardAdapter debtAdapter;

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

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);

        transactionRepository = new TransactionRepository(requireActivity().getApplication());

        UserRepository userRepository = new UserRepository(requireActivity());
        userId = (userRepository.getLoggedInUser() != null) ? userRepository.getLoggedInUser().id : -1;

        // --- Init views ---
        MaterialToolbar tb = view.findViewById(R.id.toolbar);

        tvUserName = view.findViewById(R.id.tvUserName);
        tvWalletOwner = view.findViewById(R.id.tv_wallet_owner);

        tvWalletBalance = view.findViewById(R.id.tv_wallet_balance);
        spinnerWallet = view.findViewById(R.id.spinner_wallet);
        tvTotalWalletBalance = view.findViewById(R.id.tv_total_wallet_balance);

        // ✅ RecyclerView debt cards (nếu XML có)
        rvDebtCards = view.findViewById(R.id.rv_debt_cards);
        setupDebtCards();

        // --- user name ---
        observeUserName();

        // --- wallet ---
        setupWalletSpinner();
        observeWalletsUsingExistingViewModel();

        // --- events ---
        View btnReload = view.findViewById(R.id.btnReload);
        if (btnReload != null) btnReload.setOnClickListener(v -> requireActivity().recreate());

        View btnNotification = view.findViewById(R.id.btnNotification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), NotificationActivity.class))
            );
        }

        View tvDebtDetail = view.findViewById(R.id.tvDebtDetail);
        if (tvDebtDetail != null) {
            tvDebtDetail.setOnClickListener(v ->
                    startActivity(new Intent(getActivity(), LoanTrackingActivity.class))
            );
        }

        // --- chart (real data) ---
        View barIncome = view.findViewById(R.id.barIncome);
        View barSpending = view.findViewById(R.id.barSpending);
        TextView y0 = view.findViewById(R.id.tvY0);
        TextView y25 = view.findViewById(R.id.tvY25);
        TextView y50 = view.findViewById(R.id.tvY50);
        TextView y75 = view.findViewById(R.id.tvY75);
        TextView y100 = view.findViewById(R.id.tvY100);
        TextView tvIncomeAmount = view.findViewById(R.id.tvIncomeAmount);
        TextView tvSpendingAmount = view.findViewById(R.id.tvSpendingAmount);

        int chartMaxDp = 180;
        float density = getResources().getDisplayMetrics().density;

        LocalDate now = LocalDate.now();
        transactionRepository.getTransactionsByMonth(userId, now)
                .observe(getViewLifecycleOwner(), list -> {
                    float income = 0f;
                    float spending = 0f;

                    if (list != null) {
                        for (Transaction t : list) {
                            if (t == null || t.type == null) continue;
                            if (t.type == TxType.INCOME) income += t.amount;
                            else if (t.type == TxType.EXPENSE) spending += t.amount;
                        }
                    }

                    float maxValue = Math.max(income, spending);
                    if (maxValue == 0) maxValue = 1f;

                    if (barIncome != null) {
                        int incomeHeight = Math.round(chartMaxDp * (income / maxValue) * density);
                        LinearLayout.LayoutParams lpIncome = (LinearLayout.LayoutParams) barIncome.getLayoutParams();
                        lpIncome.height = incomeHeight;
                        barIncome.setLayoutParams(lpIncome);
                    }

                    if (barSpending != null) {
                        int spendingHeight = Math.round(chartMaxDp * (spending / maxValue) * density);
                        LinearLayout.LayoutParams lpSpending = (LinearLayout.LayoutParams) barSpending.getLayoutParams();
                        lpSpending.height = spendingHeight;
                        barSpending.setLayoutParams(lpSpending);
                    }

                    if (y0 != null && y25 != null && y50 != null && y75 != null && y100 != null) {
                        if (maxValue < 1_000_000f) {
                            y0.setText("0");
                            y25.setText(String.format("%.0fk", (maxValue * 0.25f) / 1_000f));
                            y50.setText(String.format("%.0fk", (maxValue * 0.50f) / 1_000f));
                            y75.setText(String.format("%.0fk", (maxValue * 0.75f) / 1_000f));
                            y100.setText(String.format("%.0fk", maxValue / 1_000f));
                        } else {
                            y0.setText("0");
                            y25.setText(String.format("%.1fm", (maxValue * 0.25f) / 1_000_000f));
                            y50.setText(String.format("%.1fm", (maxValue * 0.50f) / 1_000_000f));
                            y75.setText(String.format("%.1fm", (maxValue * 0.75f) / 1_000_000f));
                            y100.setText(String.format("%.1fm", maxValue / 1_000_000f));
                        }
                    }

                    if (tvIncomeAmount != null) tvIncomeAmount.setText(formatMoneyDouble(income) + " đ");
                    if (tvSpendingAmount != null) tvSpendingAmount.setText(formatMoneyDouble(spending) + " đ");
                });

        // --- month range text (giữ nguyên nếu bạn đang dùng chỗ khác) ---
        Calendar c = Calendar.getInstance();
        int yy = c.get(Calendar.YEAR);
        int mm = c.get(Calendar.MONTH);

        Calendar start = Calendar.getInstance();
        start.set(yy, mm, 1);
        Calendar end = Calendar.getInstance();
        end.set(yy, mm, end.getActualMaximum(Calendar.DAY_OF_MONTH));

        // --- recent transactions ---
        View tvSeeAll = view.findViewById(R.id.tvSeeAll);
        if (tvSeeAll != null) {
            tvSeeAll.setOnClickListener(v ->
                    startActivity(new Intent(getActivity(), Transactions.class))
            );
        }

        RecyclerView rvRecent = view.findViewById(R.id.rv_recent_transactions);
        TextView tvEmpty = view.findViewById(R.id.tv_empty_recent);

        if (rvRecent != null) {
            rvRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
            TransactionAdapter recentAdapter = new TransactionAdapter(requireContext());
            rvRecent.setAdapter(recentAdapter);
            loadRecentTransactions(recentAdapter, tvEmpty);
        }
    }

    // ===================== WALLET =====================

    private void setupWalletSpinner() {
        walletNameAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        walletNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if (spinnerWallet != null) spinnerWallet.setAdapter(walletNameAdapter);

        if (spinnerWallet != null) {
            spinnerWallet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                    if (position >= 0 && position < walletList.size()) {
                        Wallet w = walletList.get(position);
                        if (tvWalletBalance != null) tvWalletBalance.setText(formatMoneyDouble(w.balance) + " đ");
                    } else {
                        if (tvWalletBalance != null) tvWalletBalance.setText("0 đ");
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }
    }

    private void observeWalletsUsingExistingViewModel() {
        walletViewModel.wallets.observe(getViewLifecycleOwner(), wallets -> {
            walletList.clear();
            walletNameAdapter.clear();

            if (wallets != null && !wallets.isEmpty()) {
                walletList.addAll(wallets);
                for (Wallet w : wallets) walletNameAdapter.add(w.name);
                if (tvWalletBalance != null) tvWalletBalance.setText(formatMoneyDouble(walletList.get(0).balance) + " đ");
            } else {
                walletNameAdapter.add("Chưa có ví");
                if (tvWalletBalance != null) tvWalletBalance.setText("0 đ");
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
        if (tvUserName == null || tvWalletOwner == null) return;

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
        if (!isAdded() || getActivity() == null) return;

        if (userId <= 0) {
            adapter.setData(Collections.emptyList());
            if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        LocalDate now = LocalDate.now();

        transactionRepository.getTransactionsByMonth(userId, now)
                .observe(getViewLifecycleOwner(), list -> {
                    if (list == null || list.isEmpty()) {
                        adapter.setData(Collections.emptyList());
                        if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        adapter.setData(list.subList(0, Math.min(3, list.size())));
                        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                    }
                });
    }

    // ===================== DEBT CARDS (item_budget as RecyclerView) =====================

    private void setupDebtCards() {
        if (rvDebtCards == null) return;

        rvDebtCards.setLayoutManager(new LinearLayoutManager(requireContext()));
        debtAdapter = new DebtCardAdapter(tx -> {
            // Click thẻ -> mở LoanTrackingActivity và lọc theo nhóm
            Intent i = new Intent(getActivity(), LoanTrackingActivity.class);

            // Dùng TxType để biết nhóm nào:
            // BORROW -> (BORROW, LOAN_REPAYMENT)
            // LEND   -> (LEND, DEBT_COLLECTION)
            i.putExtra(LoanTrackingActivity.EXTRA_DEBT_KIND,
                    tx.type == TxType.BORROW ? "BORROW" : "LEND");

            startActivity(i);
        });

        rvDebtCards.setAdapter(debtAdapter);

        loadDebtCards();
    }

    private void loadDebtCards() {
        if (debtAdapter == null) return;

        if (userId <= 0) {
            debtAdapter.setData(Collections.emptyList());
            return;
        }

        // ✅ lấy tất cả giao dịch vay/nợ (4 loại) từ repo hiện tại của bạn
        transactionRepository.getLoanTransactions(userId)
                .observe(getViewLifecycleOwner(), list -> {
                    if (list == null) list = Collections.emptyList();

                    long borrowTotal = 0L;     // y
                    long repayTotal = 0L;      // x

                    long lendTotal = 0L;       // y
                    long collectTotal = 0L;    // x

                    boolean hasBorrowGroup = false;
                    boolean hasLendGroup = false;

                    for (Transaction t : list) {
                        if (t == null || t.type == null) continue;

                        switch (t.type) {
                            case BORROW:
                                hasBorrowGroup = true;
                                borrowTotal += Math.abs(t.amount);
                                break;
                            case LOAN_REPAYMENT:
                                hasBorrowGroup = true;
                                repayTotal += Math.abs(t.amount);
                                break;
                            case LEND:
                                hasLendGroup = true;
                                lendTotal += Math.abs(t.amount);
                                break;
                            case DEBT_COLLECTION:
                                hasLendGroup = true;
                                collectTotal += Math.abs(t.amount);
                                break;
                        }
                    }

                    ArrayList<Transaction> cards = new ArrayList<>(2);

                    if (hasBorrowGroup) {
                        Transaction cardBorrow = new Transaction();
                        cardBorrow.type = TxType.BORROW;
                        cardBorrow.amount = borrowTotal;           // y
                        cardBorrow.note = String.valueOf(repayTotal); // x (nhét vào note)
                        cards.add(cardBorrow);
                    }

                    if (hasLendGroup) {
                        Transaction cardLend = new Transaction();
                        cardLend.type = TxType.LEND;
                        cardLend.amount = lendTotal;                 // y
                        cardLend.note = String.valueOf(collectTotal); // x
                        cards.add(cardLend);
                    }

                    debtAdapter.setData(cards);
                });
    }

    // Adapter dùng item_budget, nhưng model vẫn là Transaction (không tạo model riêng)
    private static class DebtCardAdapter extends RecyclerView.Adapter<DebtCardAdapter.VH> {

        interface ClickListener {
            void onClick(Transaction tx);
        }

        private final List<Transaction> data = new ArrayList<>();
        private final ClickListener clickListener;

        DebtCardAdapter(ClickListener clickListener) {
            this.clickListener = clickListener;
        }

        void setData(List<Transaction> list) {
            data.clear();
            if (list != null) data.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Transaction card = data.get(position);

            long y = Math.abs(card.amount);
            long x = 0L;
            try {
                x = Long.parseLong(card.note == null ? "0" : card.note);
            } catch (Exception ignored) {}

            if (card.type == TxType.BORROW) {
                h.ivCategoryIcon.setImageResource(R.drawable.ic_cat_debt_return);
                h.tvBudgetName.setText("ĐI VAY");
                h.tvCategories.setText("Đi vay - Đã trả");
                h.tvSpentInfo.setText("Đã trả " + formatMoneyLong(x) + " đ / " + formatMoneyLong(y) + " đ");
            } else {
                h.ivCategoryIcon.setImageResource(R.drawable.ic_cat_lend);
                h.tvBudgetName.setText("CHO VAY");
                h.tvCategories.setText("Cho vay - Đã thu hồi");
                h.tvSpentInfo.setText("Đã thu hồi " + formatMoneyLong(x) + " đ / " + formatMoneyLong(y) + " đ");
            }

            h.itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onClick(card);
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            ImageView ivCategoryIcon;
            TextView tvBudgetName, tvCategories, tvSpentInfo;

            VH(@NonNull View itemView) {
                super(itemView);
                ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
                tvBudgetName = itemView.findViewById(R.id.tvBudgetName);
                tvCategories = itemView.findViewById(R.id.tvCategories);
                tvSpentInfo = itemView.findViewById(R.id.tvSpentInfo);
            }
        }

        private static String formatMoneyLong(long value) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator('.');
            return new DecimalFormat("#,###", symbols).format(value);
        }
    }

    // ===================== HELPERS =====================

    private String formatMoneyDouble(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        return new DecimalFormat("#,###", symbols).format(value);
    }
}
