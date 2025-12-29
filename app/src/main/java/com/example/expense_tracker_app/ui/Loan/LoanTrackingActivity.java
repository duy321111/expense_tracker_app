package com.example.expense_tracker_app.ui.Loan;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.TxType;
import com.example.expense_tracker_app.data.repository.TransactionRepository;
import com.example.expense_tracker_app.ui.adapter.TransactionAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoanTrackingActivity extends AppCompatActivity {

    public static final String EXTRA_DEBT_KIND = "debt_kind"; // "BORROW" hoặc "LEND"

    private MaterialToolbar toolbar;
    private RecyclerView rvTransactions;

    private TransactionAdapter txAdapter;
    private TransactionRepository repo;

    private int userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_tracking);

        userId = getSharedPreferences("session", MODE_PRIVATE).getInt("user_id", -1);

        toolbar = findViewById(R.id.toolbar);
        rvTransactions = findViewById(R.id.rvTransactions);

        setupToolbarBack();

        repo = new TransactionRepository(getApplication());

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        txAdapter = new TransactionAdapter(this);
        rvTransactions.setAdapter(txAdapter);

        loadByIntentKind();
    }

    private void setupToolbarBack() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v ->
                    getOnBackPressedDispatcher().onBackPressed()
            );
        }
    }

    private void loadByIntentKind() {
        if (userId <= 0) {
            txAdapter.setData(Collections.emptyList());
            return;
        }

        String kind = getIntent().getStringExtra(EXTRA_DEBT_KIND); // "BORROW" or "LEND"

        List<TxType> types = new ArrayList<>();

        if ("BORROW".equals(kind)) {
            if (toolbar != null) toolbar.setTitle("Đi vay");
            types.add(TxType.BORROW);
            types.add(TxType.LOAN_REPAYMENT);
        } else if ("LEND".equals(kind)) {
            if (toolbar != null) toolbar.setTitle("Cho vay");
            types.add(TxType.LEND);
            types.add(TxType.DEBT_COLLECTION);
        } else {
            // fallback: hiển thị tất cả vay/nợ
            if (toolbar != null) toolbar.setTitle("Vay nợ");
            types.add(TxType.BORROW);
            types.add(TxType.LOAN_REPAYMENT);
            types.add(TxType.LEND);
            types.add(TxType.DEBT_COLLECTION);
        }

        repo.getTransactionsByTypes(userId, types).observe(this, list -> {
            if (list == null) list = Collections.emptyList();
            txAdapter.setData(list);
        });
    }
}
