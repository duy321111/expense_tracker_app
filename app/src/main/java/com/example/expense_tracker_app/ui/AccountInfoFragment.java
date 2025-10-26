package com.example.expense_tracker_app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.expense_tracker_app.R;

public class AccountInfoFragment extends Fragment {

    // Khai báo các thành phần UI (sẽ được tìm trong onViewCreated)
    private Button btnChangePassword;
    private Button btnSave;
    private ImageView btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gắn layout XML vào file logic này
        return inflater.inflate(R.layout.fragment_account_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Tìm các View bằng ID
        btnChangePassword = view.findViewById(R.id.btn_change_password);
        btnSave = view.findViewById(R.id.btn_save);
        btnBack = view.findViewById(R.id.btn_back);
        // TODO: Tìm các EditText (et_name, et_email, et_password) nếu cần lấy dữ liệu

        // 2. Gán sự kiện click cho nút "Đổi mật khẩu"
        btnChangePassword.setOnClickListener(v -> {
            // Điều hướng đến màn hình đổi mật khẩu, sử dụng action đã định nghĩa trong nav_graph.xml
            NavHostFragment.findNavController(AccountInfoFragment.this)
                    .navigate(R.id.action_accountInfoFragment_to_changePasswordFragment);
        });

        // 3. Gán sự kiện click cho nút "Lưu thông tin"
        btnSave.setOnClickListener(v -> {
            // TODO: Thêm logic lưu thông tin (lấy text từ EditTexts)

            // Thông báo lưu thành công (ví dụ)
            Toast.makeText(getContext(), "Đã lưu thông tin", Toast.LENGTH_SHORT).show();
        });

        // 4. Gán sự kiện click cho nút "Quay lại"
        btnBack.setOnClickListener(v -> {
            // Quay lại màn hình trước đó
            NavHostFragment.findNavController(AccountInfoFragment.this).popBackStack();
        });
    }
}