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
import com.example.expense_tracker_app.data.repository.UserRepository;
import com.example.expense_tracker_app.ui.Auth.Login;
import com.example.expense_tracker_app.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private int userId;

    private ImageView imgAvatar;
    private TextView tvName, tvEmail;
    private View btnAccountInfo, btnTransactionHistory, btnMyWallets, btnTermsPrivacy, btnReferFriend, btnRateApp, btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        
        // Lấy userId của người dùng hiện tại
        UserRepository userRepository = new UserRepository(getActivity());
        userId = userRepository.getLoggedInUser() != null ? userRepository.getLoggedInUser().id : -1;
        
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
        btnTermsPrivacy = view.findViewById(R.id.btn_terms_privacy);
        btnReferFriend = view.findViewById(R.id.btn_refer_friend);
        btnRateApp = view.findViewById(R.id.btn_rate_app);
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    private void observeUserData() {
        viewModel.getUser(userId).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                if (user.fullName != null && !user.fullName.isEmpty()) tvName.setText(user.fullName);
                else tvName.setText("Người dùng");

                if (user.email != null) tvEmail.setText(user.email);

                // Hiển thị ảnh
                if (user.profileImagePath != null && !user.profileImagePath.isEmpty()) {
                    try {
                        // Vô hiệu hóa cache bằng cách gán null trước (nếu cần thiết)
                        imgAvatar.setImageDrawable(null);
                        imgAvatar.setImageURI(Uri.parse(user.profileImagePath));
                    } catch (Exception e) {
                        imgAvatar.setImageResource(R.drawable.cute); // Ảnh mặc định
                    }
                } else {
                    imgAvatar.setImageResource(R.drawable.cute);
                }
            }
        });
    }

    private void setupEvents() {
        btnAccountInfo.setOnClickListener(v -> startActivity(new Intent(getActivity(), AccountInfoFragment.class)));
        btnTransactionHistory.setOnClickListener(v -> startActivity(new Intent(getActivity(), TransactionHistoryFragment.class)));
        btnMyWallets.setOnClickListener(v -> startActivity(new Intent(getActivity(), MyWalletsActivity.class)));

        if (btnTermsPrivacy != null) {
            btnTermsPrivacy.setOnClickListener(v -> startActivity(new Intent(getActivity(), TermsPrivacyActivity.class)));
        }

        if (btnReferFriend != null) {
            btnReferFriend.setOnClickListener(v -> {
                String githubUrl = "https://github.com/duy321111/expense_tracker_app.git";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl));
                startActivity(browserIntent);
            });
        }

        if (btnRateApp != null) {
            btnRateApp.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), FeedbackActivity.class);
                startActivity(intent);
            });
        }

        btnLogout.setOnClickListener(v -> {
            viewModel.logout();
            Intent intent = new Intent(requireActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}