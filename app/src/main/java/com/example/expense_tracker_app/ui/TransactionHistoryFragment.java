package com.example.expense_tracker_app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense_tracker_app.R;

// TODO: Bạn cần tạo TransactionAdapter.java và TransactionItem.java (model)
// import com.example.expense_tracker_app.data.TransactionAdapter;
// import com.example.expense_tracker_app.model.TransactionItem;

import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryFragment extends Fragment {

    private RecyclerView rvTransactions;
    private ImageView btnBack;
    // private TransactionAdapter adapter; // Bỏ comment khi bạn tạo Adapter

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gắn layout XML vào file logic này
        return inflater.inflate(R.layout.fragment_transaction_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Tìm Views
        rvTransactions = view.findViewById(R.id.rv_transactions);
        btnBack = view.findViewById(R.id.btn_back);

        // 2. Khởi tạo RecyclerView
        setupRecyclerView();

        // 3. Gán sự kiện cho nút "Quay lại"
        btnBack.setOnClickListener(v -> {
            NavHostFragment.findNavController(TransactionHistoryFragment.this).popBackStack();
        });

        // TODO: Thêm logic xử lý sự kiện cho các TextView chip Tháng (Tháng 1, Tháng 2...)
    }

    /**
     * Khởi tạo RecyclerView và gán Adapter giả lập
     */
    private void setupRecyclerView() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        // Tạm thời sử dụng một List trống hoặc List giả lập
        List<String> dummyData = createDummyData();

        // TODO: Thay thế bằng Adapter và Model thật của bạn
        // adapter = new TransactionAdapter(dummyData);
        // rvTransactions.setAdapter(adapter);
    }

    // Hàm tạo dữ liệu giả lập cho RecyclerView
    private List<String> createDummyData() {
        List<String> data = new ArrayList<>();
        data.add("Điện - 250.000 đ");
        data.add("Nước - 100.000 đ");
        data.add("Internet - 165.000 đ");
        data.add("Ăn uống - 50.000 đ");
        data.add("GAS - 120.000 đ");
        data.add("Thuê nhà - 1.500.000 đ");
        return data;
    }
}