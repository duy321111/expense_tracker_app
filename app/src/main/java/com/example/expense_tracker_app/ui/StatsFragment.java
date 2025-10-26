package com.example.expense_tracker_app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.adapter.TransactionAdapter;
import com.example.expense_tracker_app.data.model.TxType;
import com.example.expense_tracker_app.databinding.FragmentStatsBinding;
import com.example.expense_tracker_app.viewmodel.StatsViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class StatsFragment extends Fragment {
    private FragmentStatsBinding b;
    private StatsViewModel vm;
    private TransactionAdapter ad;
    private final int MAX_MONTHS = 4;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s){
        b = FragmentStatsBinding.inflate(i, c, false);
        vm = new ViewModelProvider(this).get(StatsViewModel.class);

        ad = new TransactionAdapter(requireContext());
        b.rvTx.setLayoutManager(new LinearLayoutManager(getContext()));
        b.rvTx.setAdapter(ad);

        renderMonthChips();
        if (vm.getMonth() == -1) vm.setMonth(3);

        // Xem chi tiết theo loại
        b.cardIncome.setOnClickListener(v -> navigateToTransactionList(TxType.INCOME));
        b.cardExpense.setOnClickListener(v -> navigateToTransactionList(TxType.EXPENSE));

        // Cập nhật UI
        vm.chart().observe(getViewLifecycleOwner(), bars -> b.barChart.setData(bars));
        vm.txs().observe(getViewLifecycleOwner(), ad::submit);
        vm.income().observe(getViewLifecycleOwner(), a -> b.tvIncome.setText(a));
        vm.expense().observe(getViewLifecycleOwner(), a -> b.tvExpense.setText(a));
        vm.month().observe(getViewLifecycleOwner(), this::updateMonthChips);

        // Bottom sheet
        b.btnMore.setOnClickListener(v -> openExportShare());

        // Chọn tháng/năm
        b.btnCalendar.setOnClickListener(v -> {
            int y = vm.getYear();
            int m = vm.getMonth() > 0 ? vm.getMonth() : java.time.LocalDate.now().getMonthValue();
            android.app.DatePickerDialog dlg = new android.app.DatePickerDialog(
                    requireContext(),
                    (view, yy, mm, dd) -> { vm.setYear(yy); vm.setMonth(mm + 1); },
                    y, m - 1, 1
            );
            dlg.show();
        });

        return b.getRoot();
    }

    private void navigateToTransactionList(TxType type) {
        int currentYear = vm.getYear();
        int currentMonth = vm.getMonth();

        Bundle bundle = new Bundle();
        bundle.putString("TX_TYPE", type.name());
        bundle.putInt("YEAR", currentYear);
        bundle.putInt("MONTH", currentMonth);

        TransactionListFragment listFragment = new TransactionListFragment();
        listFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, listFragment)
                .addToBackStack(null)
                .commit();
    }

    private void renderMonthChips(){
        b.llMonths.removeAllViews();
        int currentYear = vm.getYear();
        for(int m=1;m<=MAX_MONTHS;m++){
            TextView chip = (TextView) getLayoutInflater().inflate(R.layout.chip_month, b.llMonths, false);
            chip.setText("Tháng " + m + " " + currentYear);
            int month = m;
            chip.setOnClickListener(v -> vm.setMonth(month));
            b.llMonths.addView(chip);
        }
    }

    private void updateMonthChips(int currentMonth) {
        for (int i = 0; i < b.llMonths.getChildCount(); i++) {
            TextView chip = (TextView) b.llMonths.getChildAt(i);
            int month = i + 1;
            if (month == currentMonth) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                chip.setTextColor(requireContext().getColor(R.color.white));
            } else {
                chip.setBackgroundResource(R.drawable.bg_card);
                chip.setTextColor(requireContext().getColor(R.color.neutral_900));
            }
        }
    }

    private void openExportShare(){
        BottomSheetDialog d = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(getContext()).inflate(R.layout.sheet_export_share, null, false);
        sheet.findViewById(R.id.btnExport).setOnClickListener(v -> d.dismiss());
        sheet.findViewById(R.id.btnShare).setOnClickListener(v -> d.dismiss());
        d.setContentView(sheet); d.show();
    }
}