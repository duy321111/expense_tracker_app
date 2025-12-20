package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.ui.Auth.Login; // Import màn hình Login
import com.example.expense_tracker_app.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private int userId = 1;

    private ImageView imgAvatar;
    private TextView tvName, tvEmail;
    private View btnAccountInfo, btnTransactionHistory, btnMyWallets, btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        initViews(view);
        observeUserData();
        setupEvents();
    }

    private void initViews(View view) {
        imgAvatar = view.findViewById(R.id.img_profile_avatar);
        tvName = view.findViewById(R.id.tv_profile_name);
        tvEmail = view.findViewById(R.id.tv_profile_email);

        btnAccountInfo = view.findViewById(R.id.btn_account_info);
        btnTransactionHistory = view.findViewById(R.id.btn_transaction_history);
        btnMyWallets = view.findViewById(R.id.btn_my_wallets);
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    private void observeUserData() {
        viewModel.getUser(userId).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                if (user.fullName != null && !user.fullName.isEmpty()) tvName.setText(user.fullName);
                else tvName.setText("Người dùng");

                if (user.email != null) tvEmail.setText(user.email);

                if (user.profileImagePath != null && !user.profileImagePath.isEmpty()) {
                    try {
                        imgAvatar.setImageURI(Uri.parse(user.profileImagePath));
                    } catch (Exception e) {
                        imgAvatar.setImageResource(R.drawable.bg_icon_round_accent_1);
                    }
                } else {
                    imgAvatar.setImageResource(R.drawable.bg_icon_round_accent_1);
                }
            }
        });
    }

    private void setupEvents() {
        btnAccountInfo.setOnClickListener(v -> startActivity(new Intent(getActivity(), AccountInfoFragment.class)));

        // Lưu ý: Đảm bảo TransactionHistoryFragment/MyWalletsFragment là Activity hoặc sửa Intent cho phù hợp
        btnTransactionHistory.setOnClickListener(v -> startActivity(new Intent(getActivity(), TransactionHistoryFragment.class)));
        btnMyWallets.setOnClickListener(v -> startActivity(new Intent(getActivity(), MyWalletsFragment.class)));

        // --- SỬA PHẦN ĐĂNG XUẤT ---
        btnLogout.setOnClickListener(v -> {
            // 1. Gọi ViewModel để xóa dữ liệu lưu trữ (SharedPreferences)
            viewModel.logout();

            // 2. Chuyển về màn hình Login (activity_login)
            Intent intent = new Intent(requireActivity(), Login.class);

            // Cờ này giúp xóa sạch các màn hình cũ, người dùng không thể bấm Back để quay lại
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
        });
    }
}