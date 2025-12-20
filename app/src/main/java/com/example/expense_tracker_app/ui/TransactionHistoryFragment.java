package com.example.expense_tracker_app.ui;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.expense_tracker_app.data.model.TransactionItem;
import com.example.expense_tracker_app.ui.adapter.TransactionAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;

import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryFragment extends AppCompatActivity {

    private RecyclerView rvTransactions;
    private ImageView btnBack;
    private TransactionAdapter adapter; // Bỏ comment khi bạn tạo Adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_transaction_history); // đổi layout nếu muốn

        // 1. Tìm Views
        rvTransactions = findViewById(R.id.rv_transactions);
        btnBack = findViewById(R.id.btn_back);

        // 2. Khởi tạo RecyclerView
        setupRecyclerView();

        // 3. Gán sự kiện cho nút "Quay lại"
        btnBack.setOnClickListener(v -> finish());

        // TODO: Thêm logic xử lý sự kiện cho các TextView chip Tháng (Tháng 1, Tháng 2...)
    }

    /**
     * Khởi tạo RecyclerView và gán Adapter giả lập
     */
    private void setupRecyclerView() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));

        // Tạm thời sử dụng một List trống hoặc List giả lập
        List<TransactionItem> transactionList = createDummyData();

        // TODO: Thay thế bằng Adapter và Model thật của bạn
        adapter = new TransactionAdapter(this, transactionList);

        rvTransactions.setAdapter(adapter);
    }

    // Hàm tạo dữ liệu giả lập cho RecyclerView
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
