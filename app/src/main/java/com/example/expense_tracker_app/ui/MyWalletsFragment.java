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

public class MyWalletsFragment extends AppCompatActivity {

    private WalletViewModel viewModel;
    private WalletAdapter adapter;
    private TextView tvTotalBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_my_wallets); // Sử dụng layout chứa RecyclerView

        // 1. Ánh xạ View từ XML (Lưu ý: Dùng ID của RecyclerView)
        tvTotalBalance = findViewById(R.id.tv_total_balance);
        RecyclerView rvWallets = findViewById(R.id.rv_wallets);
        View btnBack = findViewById(R.id.btn_back);

        // ID nút thêm ví là btnAddWallet (theo XML bạn gửi)
        View btnAddNew = findViewById(R.id.btnAddWallet);

        // 2. Setup ViewModel & Adapter
        viewModel = new ViewModelProvider(this).get(WalletViewModel.class);

        // Khởi tạo Adapter và xử lý sự kiện khi click vào ví (để xóa)
        adapter = new WalletAdapter(this::showDeleteConfirmation);

        rvWallets.setLayoutManager(new LinearLayoutManager(this));
        rvWallets.setAdapter(adapter);

        // 3. Quan sát dữ liệu từ Database (LiveData)

        // Khi danh sách ví thay đổi -> Cập nhật lên RecyclerView
        viewModel.wallets.observe(this, list -> {
            adapter.setList(list);
        });

        // Khi tổng tiền thay đổi -> Cập nhật text tổng
        viewModel.totalBalance.observe(this, total -> {
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            double balance = (total != null) ? total : 0.0;
            tvTotalBalance.setText(formatter.format(balance) + " đ");
        });

        // 4. Xử lý các sự kiện Click
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnAddNew != null) {
            btnAddNew.setOnClickListener(v -> {
                // Mở màn hình thêm ví
                Intent intent = new Intent(MyWalletsFragment.this, AddWalletActivity.class);
                startActivity(intent);
            });
        }
    }

    // Hàm hiện popup hỏi xóa khi click vào ví
    private void showDeleteConfirmation(Wallet wallet) {
        new AlertDialog.Builder(this)
                .setTitle("Xoá ví?")
                .setMessage("Bạn có chắc muốn xoá ví " + wallet.name + " không? Số dư trong ví này sẽ bị mất.")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    viewModel.deleteWallet(wallet);
                    Toast.makeText(this, "Đã xoá ví", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}