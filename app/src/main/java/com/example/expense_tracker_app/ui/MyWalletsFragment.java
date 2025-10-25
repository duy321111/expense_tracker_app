package com.example.expense_tracker_app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.expense_tracker_app.R;
import java.text.NumberFormat;
import java.util.Locale;

public class MyWalletsFragment extends Fragment {

    private TextView tvTotalBalance;
    private TextView tvWalletBasic;
    private TextView tvWalletTdbank;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gắn layout XML vào file logic này
        return inflater.inflate(R.layout.fragment_my_wallets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Tìm các View bằng ID mà bạn đã định nghĩa trong XML
        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvWalletBasic = view.findViewById(R.id.tv_wallet_balance_basic);
        tvWalletTdbank = view.findViewById(R.id.tv_wallet_balance_td);

        // Tải dữ liệu và thực hiện phép tính
        loadWalletData();
    }

    private void loadWalletData() {
        // TODO: Thay thế code giả lập này bằng logic lấy dữ liệu
        //       từ ViewModel hoặc Database của bạn.
        double balanceBasic = 11458000.0;
        double balanceTdbank = 12432876.0;

        // --- ĐÂY LÀ LOGIC TÍNH TỔNG MÀ BẠN MUỐN ---
        double totalBalance = balanceBasic + balanceTdbank;

        // Định dạng tiền tệ cho VNĐ (ví dụ: 11.458.000 ₫)
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);

        // Cập nhật giao diện (UI)
        // Các giá trị "android:text" trong XML sẽ bị ghi đè bởi các giá trị này
        tvTotalBalance.setText(currencyFormatter.format(totalBalance));
        tvWalletBasic.setText(currencyFormatter.format(balanceBasic));
        tvWalletTdbank.setText(currencyFormatter.format(balanceTdbank));
    }
}