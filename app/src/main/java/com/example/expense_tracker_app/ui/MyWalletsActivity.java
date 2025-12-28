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

import java.text.NumberFormat;
import java.util.Locale;

public class MyWalletsActivity extends AppCompatActivity {

    private WalletViewModel viewModel;
    private WalletAdapter adapter;
    private TextView tvTotalBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_my_wallets);

        // 1. Ánh xạ View (Đã sửa ID cho khớp với file XML fragment_my_wallets.xml)
        tvTotalBalance = findViewById(R.id.tv_total_balance);

        // Sửa: rv_my_wallets -> rv_wallets
        RecyclerView rvWallets = findViewById(R.id.rv_wallets);

        // Sửa: btn_add_new_wallet -> btnAddWallet
        View btnAddNew = findViewById(R.id.btnAddWallet);

        // Sửa: btn_back_wallets -> btn_back
        View btnBack = findViewById(R.id.btn_back);

        // 2. Setup ViewModel & Adapter
        viewModel = new ViewModelProvider(this).get(WalletViewModel.class);

        // Pass hàm showDeleteConfirmation vào adapter để xử lý khi click item
        adapter = new WalletAdapter(this::showDeleteConfirmation);

        rvWallets.setLayoutManager(new LinearLayoutManager(this));
        rvWallets.setAdapter(adapter);

        // 3. Quan sát dữ liệu
        viewModel.wallets.observe(this, list -> {
            adapter.setList(list);
        });

        viewModel.totalBalance.observe(this, total -> {
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvTotalBalance.setText(formatter.format(total) + " đ");
        });

        // 4. Sự kiện
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        btnAddNew.setOnClickListener(v -> {
            // Kiểm tra xem đã đủ 2 ví chưa trước khi cho mở màn hình thêm
            if (adapter.getItemCount() >= 2) {
                Toast.makeText(this, "Bạn chỉ được tạo tối đa 2 ví (Tiền mặt & Chuyển khoản)", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MyWalletsActivity.this, AddWalletActivity.class);
                startActivity(intent);
            }
        });
    }

    // --- LOGIC XOÁ VÍ ---
    private void showDeleteConfirmation(Wallet wallet) {
        // 1. Kiểm tra số dư âm
        if (wallet.balance < 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Không thể xoá ví")
                    .setMessage("Ví \"" + wallet.name + "\" đang có số dư âm (" +
                            NumberFormat.getInstance(new Locale("vi", "VN")).format(wallet.balance) +
                            " đ). Vui lòng thanh toán hết nợ hoặc điều chỉnh số dư về 0 trước khi xoá.")
                    .setPositiveButton("Đã hiểu", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return; // Dừng lại, không hiện popup xoá
        }

        // 2. Nếu số dư >= 0 thì cho phép xoá bình thường
        new AlertDialog.Builder(this)
                .setTitle("Xoá ví?")
                .setMessage("Bạn có chắc muốn xoá ví \"" + wallet.name + "\" không? Tất cả giao dịch thuộc ví này sẽ bị mất.")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    viewModel.deleteWallet(wallet);
                    Toast.makeText(this, "Đã xoá ví " + wallet.name, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}