package com.example.expense_tracker_app.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker_app.R;

import java.text.NumberFormat;
import java.util.Locale;

public class MyWalletsFragment extends AppCompatActivity {

    private TextView tvTotalBalance;
    private TextView tvWalletBasic;
    private TextView tvWalletTdbank;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_my_wallets); // đổi layout nếu muốn

        // 1. Tìm các View bằng ID từ file XML
        tvTotalBalance = findViewById(R.id.tv_total_balance);
        tvWalletBasic = findViewById(R.id.tv_wallet_balance_basic);
        tvWalletTdbank = findViewById(R.id.tv_wallet_balance_td);
        btnBack = findViewById(R.id.btn_back);

        // 2. Tải dữ liệu và thực hiện phép tính
        loadWalletData();

        // 3. Gán sự kiện cho nút "Quay lại"
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Hàm lấy dữ liệu, CỘNG TỔNG TIỀN, và cập nhật UI
     */
    private void loadWalletData() {
        // TODO: Thay thế code giả lập này bằng logic lấy dữ liệu
        //       từ ViewModel hoặc Database (Firebase) của bạn.
        double balanceBasic = 11458000.0;
        double balanceTdbank = 12432876.0;

        // --- ĐÂY LÀ LOGIC TÍNH TỔNG ---
        double totalBalance = balanceBasic + balanceTdbank;

        // Định dạng tiền tệ cho VNĐ (ví dụ: 11.458.000 ₫)
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);

        // Cập nhật giao diện (UI)
        tvTotalBalance.setText(currencyFormatter.format(totalBalance));
        tvWalletBasic.setText(currencyFormatter.format(balanceBasic));
        tvWalletTdbank.setText(currencyFormatter.format(balanceTdbank));
    }
}
