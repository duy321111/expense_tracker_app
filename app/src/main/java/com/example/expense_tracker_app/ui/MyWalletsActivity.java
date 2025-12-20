package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Wallet;
import com.example.expense_tracker_app.ui.adapter.WalletAdapter;
import com.example.expense_tracker_app.viewmodel.WalletViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import java.text.NumberFormat;
import java.util.Locale;

public class MyWalletsActivity extends AppCompatActivity {

    private WalletViewModel viewModel;
    private WalletAdapter adapter;
    private TextView tvTotalBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_my_wallets); // Tên layout khớp với ảnh bạn gửi

        // 1. Ánh xạ View
        tvTotalBalance = findViewById(R.id.tv_total_balance); // ID của text hiện tổng tiền
        RecyclerView rvWallets = findViewById(R.id.rv_wallets); // ID của RecyclerView
        Button btnAddNew = findViewById(R.id.btnAddWallet); // ID nút Thêm ví mới
        View btnBack = findViewById(R.id.btn_back); // Nút back (nếu là ImageButton/View)

        // 2. Setup ViewModel & Adapter
        viewModel = new ViewModelProvider(this).get(WalletViewModel.class);
        adapter = new WalletAdapter(this::showDeleteConfirmation); // Truyền hàm xử lý click

        rvWallets.setLayoutManager(new LinearLayoutManager(this));
        rvWallets.setAdapter(adapter);

        // 3. Quan sát dữ liệu (LiveData)
        viewModel.wallets.observe(this, list -> {
            adapter.setList(list); // Cập nhật list
        });

        viewModel.totalBalance.observe(this, total -> {
            // Cập nhật tổng tiền
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvTotalBalance.setText(formatter.format(total) + " đ");
        });

        // 4. Sự kiện
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        btnAddNew.setOnClickListener(v -> {
            // Mở lại AddWalletActivity nhưng không có cờ FirstRun
            Intent intent = new Intent(MyWalletsActivity.this, AddWalletActivity.class);
            startActivity(intent);
        });
    }

    // Hiện popup xác nhận xoá
    private void showDeleteConfirmation(Wallet wallet) {
        new AlertDialog.Builder(this)
                .setTitle("Xoá ví?")
                .setMessage("Bạn có chắc muốn xoá ví " + wallet.name + " không? Dữ liệu giao dịch liên quan có thể bị ảnh hưởng.")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    viewModel.deleteWallet(wallet);
                    Toast.makeText(this, "Đã xoá ví", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}