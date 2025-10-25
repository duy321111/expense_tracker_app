package com.example.expense_tracker_app.ui;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.adapter.TransactionAdapter;
import com.example.expense_tracker_app.data.model.TxType; // Cần import TxType
import com.example.expense_tracker_app.databinding.FragmentStatsBinding;
import com.example.expense_tracker_app.viewmodel.StatsViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class StatsFragment extends Fragment {
    private FragmentStatsBinding b;
    private StatsViewModel vm;
    private TransactionAdapter ad;
    private final int MAX_MONTHS = 4;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s){
        b = FragmentStatsBinding.inflate(i, c, false);
        vm = new ViewModelProvider(this).get(StatsViewModel.class);

        ad = new TransactionAdapter(requireContext());
        b.rvTx.setLayoutManager(new LinearLayoutManager(getContext()));
        b.rvTx.setAdapter(ad);

        // Khởi tạo chips và set tháng mặc định
        renderMonthChips();
        if (vm.getMonth() == -1) vm.setMonth(3);

        // 1. Logic xem chi tiết (FIXED: Đã chuyển lên trước return)
        b.cardIncome.setOnClickListener(v -> navigateToTransactionList(TxType.INCOME));
        b.cardExpense.setOnClickListener(v -> navigateToTransactionList(TxType.EXPENSE));

        // 2. Logic cập nhật UI
        vm.chart().observe(getViewLifecycleOwner(), bars -> b.barChart.setData(bars));
        vm.txs().observe(getViewLifecycleOwner(), ad::submit);
        vm.income().observe(getViewLifecycleOwner(), a -> b.tvIncome.setText(a));
        vm.expense().observe(getViewLifecycleOwner(), a -> b.tvExpense.setText(a));
        vm.month().observe(getViewLifecycleOwner(), month -> updateMonthChips(month));

        // 3. Các nút khác
        b.btnMore.setOnClickListener(v -> openExportShare());

        // LỖI CODE GỐC: Dòng 'b.cardIncome.setOnClickListener' và 'b.cardExpense.setOnClickListener'
        // đã bị đặt sau 'return b.getRoot()', khiến chúng không bao giờ được thực thi.
        // ĐÃ SỬA: Logic đã được chuyển lên trên.

        return b.getRoot();
    }

    // Hàm điều hướng đã được cải tiến để truyền đủ Year và Month
    private void navigateToTransactionList(TxType type) {
        // 1. Lấy thông tin tháng và năm hiện tại từ ViewModel
        int currentYear = vm.getYear();
        int currentMonth = vm.getMonth();

        // 2. Tạo Bundle để truyền dữ liệu
        Bundle bundle = new Bundle();
        bundle.putString("TX_TYPE", type.name());
        bundle.putInt("YEAR", currentYear);
        bundle.putInt("MONTH", currentMonth);

        // 3. Khởi tạo Fragment và thiết lập đối số
        TransactionListFragment listFragment = new TransactionListFragment();
        listFragment.setArguments(bundle);

        // 4. Thực hiện giao dịch Fragment
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, listFragment)
                .addToBackStack(null)
                .commit();
    }

    private void renderMonthChips(){
        b.llMonths.removeAllViews();
        // Cải tiến: Hiển thị năm hiện tại trên chip
        int currentYear = vm.getYear();
        for(int m=1;m<=MAX_MONTHS;m++){
            TextView chip = (TextView) getLayoutInflater().inflate(R.layout.chip_month, b.llMonths, false);
            chip.setText("Tháng " + m + " " + currentYear); // Thêm năm vào chip
            int month = m;
            chip.setOnClickListener(v -> vm.setMonth(month));
            b.llMonths.addView(chip);
        }
    }

    private void updateMonthChips(int currentMonth) {
        // ... (Logic cập nhật chip, đã sửa getResources().getColor)
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