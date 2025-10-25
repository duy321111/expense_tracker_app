// Đây là file logic cho fragment_my_wallets.xml
package com.example.expense_tracker_app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.expense_tracker_app.R;
import java.text.NumberFormat;
import java.util.Locale;

public class MyWalletsFragment extends Fragment {

    private TextView tvTotalBalance;
    private TextView tvWalletBasic;
    private TextView tvWalletTdbank;
    private ImageView btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gắn layout XML (fragment_my_wallets.xml) vào file logic này
        return inflater.inflate(R.layout.fragment_my_wallets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Tìm các View bằng ID từ file XML
        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvWalletBasic = view.findViewById(R.id.tv_wallet_balance_basic);
        tvWalletTdbank = view.findViewById(R.id.tv_wallet_balance_td);
        btnBack = view.findViewById(R.id.btn_back);

        // 2. Tải dữ liệu và thực hiện phép tính
        loadWalletData();

        // 3. Gán sự kiện cho nút "Quay lại"
        btnBack.setOnClickListener(v -> {
            NavHostFragment.findNavController(MyWalletsFragment.this).popBackStack();
        });
    }

    /**
     * Hàm lấy dữ liệu, CỘNG TỔNG TIỀN, và cập nhật UI
     */
    private void loadWalletData() {
        // TODO: Thay thế code giả lập này bằng logic lấy dữ liệu
        //       từ ViewModel hoặc Database (Firebase) của bạn.
        double balanceBasic = 11458000.0;
        double balanceTdbank = 12432876.0;

        // --- ĐÂY LÀ LOGIC TÍNH TỔNG MÀ BẠN MUỐN ---
        double totalBalance = balanceBasic + balanceTdbank;

        // Định dạng tiền tệ cho VNĐ (ví dụ: 11.458.000 ₫)
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);

        // Cập nhật giao diện (UI)
        // Các giá trị text trong XML sẽ bị ghi đè bởi các giá trị này
        tvTotalBalance.setText(currencyFormatter.format(totalBalance));
        tvWalletBasic.setText(currencyFormatter.format(balanceBasic));
        tvWalletTdbank.setText(currencyFormatter.format(balanceTdbank));
    }
}

