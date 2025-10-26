package com.example.expense_tracker_app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.expense_tracker_app.R;

public class ProfileFragment extends Fragment {

    // Khai báo các nút (TextView)
    private TextView btnAccountInfo, btnTransactionHistory, btnMyWallets, btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gắn layout XML vào file logic này
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Tìm các View bằng ID
        btnAccountInfo = view.findViewById(R.id.btn_account_info);
        btnTransactionHistory = view.findViewById(R.id.btn_transaction_history);
        btnMyWallets = view.findViewById(R.id.btn_my_wallets);
        btnLogout = view.findViewById(R.id.btn_logout);

        // 2. Gán sự kiện cho nút "Thông tin tài khoản"
        btnAccountInfo.setOnClickListener(v -> {
            // Điều hướng đến AccountInfoFragment
            NavHostFragment.findNavController(ProfileFragment.this)
                    .navigate(R.id.action_profileFragment_to_accountInfoFragment);
        });

        // 3. Gán sự kiện cho nút "Lịch sử giao dịch"
        btnTransactionHistory.setOnClickListener(v -> {
            // Điều hướng đến TransactionHistoryFragment
            NavHostFragment.findNavController(ProfileFragment.this)
                    .navigate(R.id.action_profileFragment_to_transactionHistoryFragment);
        });

        // 4. Gán sự kiện cho nút "Ví của bạn"
        btnMyWallets.setOnClickListener(v -> {
            // Điều hướng đến MyWalletsFragment
            NavHostFragment.findNavController(ProfileFragment.this)
                    .navigate(R.id.action_profileFragment_to_myWalletsFragment);
        });

        // 5. Gán sự kiện cho nút "Đăng xuất"
        btnLogout.setOnClickListener(v -> {
            // TODO: Thêm logic đăng xuất thật (xóa token/session)
            Toast.makeText(getContext(), "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
            // Điều hướng về màn hình Login (tùy thuộc vào nav_graph của bạn)
        });

        // TODO: Thêm logic cho các nút còn lại (Mã bảo vệ, Face ID, v.v.)
    }
}