package com.example.expense_tracker_app.ui;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.adapter.TransactionAdapter;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.TxType;
import com.example.expense_tracker_app.data.repository.InMemoryRepo;
import com.example.expense_tracker_app.data.repository.Repository;
import com.example.expense_tracker_app.databinding.FragmentTransactionListBinding; // Assume you create this binding

import java.util.List;
import java.util.stream.Collectors;

public class TransactionListFragment extends Fragment {
    private FragmentTransactionListBinding b; // Assume you create this binding
    private final Repository repo = new InMemoryRepo();
    private TransactionAdapter ad;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s){
        // Assuming you create a layout named fragment_transaction_list.xml
        b = FragmentTransactionListBinding.inflate(i, c, false);
        ad = new TransactionAdapter(requireContext());

        b.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        b.rvList.setAdapter(ad);

        if (getArguments() != null) {
            String typeString = getArguments().getString("TX_TYPE");
            int year = getArguments().getInt("YEAR");
            int month = getArguments().getInt("MONTH");
            TxType type = TxType.valueOf(typeString);

            // 1. Load all transactions for the month
            List<Transaction> allTxs = repo.transactionsByMonth(year, month);

            // 2. Filter transactions by the specified type (INCOME or EXPENSE)
            List<Transaction> filteredTxs = allTxs.stream()
                    .filter(tx -> tx.type == type)
                    .collect(Collectors.toList());

            // 3. Set Title and submit data
            String title = (type == TxType.INCOME) ? "Thu nhập" : "Chi tiêu";
            b.tvTitle.setText(title);

            ad.submit(filteredTxs);
        }

        // Handle Back button (optional, depends on your layout structure)
        b.btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return b.getRoot();
    }
}