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

// SỬA: Thêm các import mới
import com.example.expense_tracker_app.data.model.TransactionItem;
import com.example.expense_tracker_app.ui.adapter.TransactionAdapter;

import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryFragment extends Fragment {

    private RecyclerView rvTransactions;
    private ImageView btnBack;
    private TransactionAdapter adapter; // Bỏ comment và sử dụng adapter thật

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

    private void setupRecyclerView() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        // SỬA: Lấy dữ liệu giả lập là List<TransactionItem>
        List<TransactionItem> transactionList = createDummyData();

        // SỬA: Khởi tạo và gán Adapter thật
        adapter = new TransactionAdapter(requireContext(), transactionList);
        rvTransactions.setAdapter(adapter);
    }

    // SỬA: Hàm tạo dữ liệu giả lập phải trả về List<TransactionItem>
    private List<TransactionItem> createDummyData() {
        List<TransactionItem> data = new ArrayList<>();

        // Dữ liệu giả lập (Giống hình ảnh bạn cung cấp)
        // Tham chiếu R.drawable.ic_... phải là tên file icon chính xác của bạn
        data.add(new TransactionItem("Điện", "Tiền mặt", "-250.000 đ", R.drawable.ic_settings, true));
        data.add(new TransactionItem("Nước", "Tiền mặt", "-100.000 đ", R.drawable.ic_close, true));
        data.add(new TransactionItem("Internet", "Chuyển khoản", "-165.000 đ", R.drawable.ic_notification, true));
        data.add(new TransactionItem("Ăn uống", "Tiền mặt", "-50.000 đ", R.drawable.ic_food, true));

        // Thêm một giao dịch thu nhập để kiểm tra màu sắc
        data.add(new TransactionItem("Lương", "Ngân hàng", "+15.000.000 đ", R.drawable.ic_money_bag, false));

        data.add(new TransactionItem("GAS", "Tiền mặt", "-120.000 đ", R.drawable.ic_fooddrinks, true));
        data.add(new TransactionItem("Thuê nhà", "Chuyển khoản", "-1.500.000 đ", R.drawable.ic_rent_house, true));

        return data;
    }
}