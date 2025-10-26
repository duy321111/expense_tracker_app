package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.expense_tracker_app.R;

public class ProfileFragment extends Fragment {

    // Khai báo các nút
    private View btnAccountInfo;
    private View btnTransactionHistory;
    private View btnMyWallets;
    private View btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- 1. Ánh xạ các nút bấm ---
        btnAccountInfo = view.findViewById(R.id.btn_account_info);
        btnTransactionHistory = view.findViewById(R.id.btn_transaction_history);
        btnMyWallets = view.findViewById(R.id.btn_my_wallets);
        btnLogout = view.findViewById(R.id.btn_logout);

        // --- 2. Gán hành động click ---
        btnAccountInfo.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AccountInfoFragment.class)));

        btnTransactionHistory.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), TransactionHistoryFragment.class)));

        btnMyWallets.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), MyWalletsFragment.class)));

        btnLogout.setOnClickListener(v -> {
            // 1. Khởi tạo Intent tới Login
            Intent intent = new Intent(requireActivity().getApplicationContext(), Login.class);

            // 2. Xóa stack Activity
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            // 3. Bắt đầu Activity Login
            requireActivity().startActivity(intent);

            // 4. Kết thúc Activity cha
            requireActivity().finish();
        });
    }
}
