package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.expense_tracker_app.R;

public class ProfileFragment extends Fragment {

    // SỬA: Khai báo các biến View (như các nút bấm) ở đây
    private View btnAccountInfo;
    private View btnTransactionHistory;
    private View btnMyWallets;
    private View btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- 1. ÁNH XẠ CÁC NÚT BẤM ---
        // Ánh xạ các biến đã khai báo ở đầu class
        btnAccountInfo = view.findViewById(R.id.btn_account_info);
        btnTransactionHistory = view.findViewById(R.id.btn_transaction_history);
        btnMyWallets = view.findViewById(R.id.btn_my_wallets);
        btnLogout = view.findViewById(R.id.btn_logout); // Đã ánh xạ nút Đăng xuất


        // --- 2. GÁN HÀNH ĐỘNG CLICK ĐỂ ĐIỀU HƯỚNG ---

        // Trỏ tới "Account Info"
        btnAccountInfo.setOnClickListener(v -> {
            NavHostFragment.findNavController(ProfileFragment.this)
                    .navigate(R.id.action_profileFragment_to_accountInfoFragment);
        });

        // Trỏ tới "Transaction History"
        btnTransactionHistory.setOnClickListener(v -> {
            NavHostFragment.findNavController(ProfileFragment.this)
                    .navigate(R.id.action_profileFragment_to_transactionHistoryFragment);
        });

        // Trỏ tới "My Wallets"
        btnMyWallets.setOnClickListener(v -> {
            NavHostFragment.findNavController(ProfileFragment.this)
                    .navigate(R.id.action_profileFragment_to_myWalletsFragment);
        });

        // Logic Đăng xuất
        btnLogout.setOnClickListener(v -> {
            // 1. Khởi tạo Intent bằng requireActivity().getApplicationContext()
            // Đảm bảo tên class Login là chính xác
            Intent intent = new Intent(requireActivity().getApplicationContext(), Login.class);

            // 2. Sử dụng cờ FLAG để xóa stack (Activity Profile và Home)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            // 3. Sử dụng requireActivity().startActivity(intent) để khởi chạy
            requireActivity().startActivity(intent);

            // 4. Kết thúc Activity cha (ProfileActivity)
            requireActivity().finish();
        });
    }
}