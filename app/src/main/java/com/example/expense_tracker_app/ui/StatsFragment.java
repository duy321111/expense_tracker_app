package com.example.expense_tracker_app.ui;

import android.os.Bundle;
import android.view.*;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.adapter.TransactionAdapter;
import com.example.expense_tracker_app.databinding.FragmentStatsBinding;
import com.example.expense_tracker_app.viewmodel.StatsViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class StatsFragment extends Fragment {
    private FragmentStatsBinding b;
    private StatsViewModel vm;
    private TransactionAdapter ad;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s){
        b = FragmentStatsBinding.inflate(i, c, false);
        vm = new ViewModelProvider(this).get(StatsViewModel.class);

        ad = new TransactionAdapter();
        b.rvTx.setLayoutManager(new LinearLayoutManager(getContext()));
        b.rvTx.setAdapter(ad);

        renderMonthChips();

        vm.chart().observe(getViewLifecycleOwner(), bars -> b.barChart.setData(bars));
        vm.txs().observe(getViewLifecycleOwner(), ad::submit);
        vm.income().observe(getViewLifecycleOwner(), a -> b.tvIncome.setText(a + " đ"));
        vm.expense().observe(getViewLifecycleOwner(), a -> b.tvExpense.setText(a + " đ"));

        b.btnMore.setOnClickListener(v -> openExportShare());
        return b.getRoot();
    }

    private void renderMonthChips(){
        b.llMonths.removeAllViews();
        for(int m=1;m<=4;m++){
            TextView chip = new TextView(getContext());
            chip.setText("Tháng " + m);
            chip.setTextSize(12f);
            chip.setPadding(28,16,28,16);
            chip.setBackgroundResource(R.drawable.bg_card);
            chip.setTextColor(getResources().getColor(R.color.text_primary));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(8,8,8,8);
            chip.setLayoutParams(lp);
            int month = m;
            chip.setOnClickListener(v -> vm.setMonth(month));
            b.llMonths.addView(chip);
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
