package com.example.expense_tracker_app.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.expense_tracker_app.R;

public class ChangePasswordFragment extends Fragment {

    // Khai báo các thành phần UI
    private EditText etOldPass, etNewPass, etConfirmPass;
    private Button btnConfirm;
    private ImageView btnBack;
    private static final int MIN_PASSWORD_LENGTH = 6; // Độ dài mật khẩu tối thiểu

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gắn layout XML vào file logic này
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Tìm các View bằng ID
        etOldPass = view.findViewById(R.id.et_old_pass);
        etNewPass = view.findViewById(R.id.et_new_pass);
        etConfirmPass = view.findViewById(R.id.et_confirm_pass);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        btnBack = view.findViewById(R.id.btn_back);

        // 2. Gán sự kiện cho nút "Xác nhận"
        btnConfirm.setOnClickListener(v -> handleChangePassword());

        // 3. Gán sự kiện cho nút "Quay lại"
        btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(ChangePasswordFragment.this).popBackStack()
        );
    }

    /**
     * Xử lý sự kiện đổi mật khẩu (Gồm Validation và Call API)
     */
    private void handleChangePassword() {
        if (!validateInput()) {
            return; // Dừng nếu đầu vào không hợp lệ
        }

        // Nếu hợp lệ, tiến hành đổi mật khẩu
        performPasswordChange();
    }

    /**
     * Thực hiện kiểm tra (Validation) cơ bản cho các trường input
     * @return true nếu tất cả hợp lệ, false nếu có lỗi
     */
    private boolean validateInput() {
        String oldPass = etOldPass.getText().toString();
        String newPass = etNewPass.getText().toString();
        String confirmPass = etConfirmPass.getText().toString();

        // 1. Reset lỗi
        etOldPass.setError(null);
        etNewPass.setError(null);
        etConfirmPass.setError(null);

        boolean isValid = true;

        // 2. Kiểm tra các trường rỗng
        if (TextUtils.isEmpty(oldPass)) {
            etOldPass.setError(getString(R.string.error_field_required));
            isValid = false;
        }
        if (TextUtils.isEmpty(newPass)) {
            etNewPass.setError(getString(R.string.error_field_required)); // SỬA LỖI
            isValid = false;
        }
        if (TextUtils.isEmpty(confirmPass)) {
            etConfirmPass.setError(getString(R.string.error_field_required)); // SỬA LỖI
            isValid = false;
        }

        // Dừng kiểm tra nếu có trường rỗng
        if (!isValid) {
            return false;
        }

        // 3. Kiểm tra độ dài mật khẩu mới
        if (newPass.length() < MIN_PASSWORD_LENGTH) { // THÊM LOGIC KIỂM TRA ĐỘ DÀI
            etNewPass.setError(getString(R.string.error_password_length, MIN_PASSWORD_LENGTH));
            isValid = false;
        }

        // 4. Kiểm tra Mật khẩu mới và Xác nhận Mật khẩu có khớp không
        if (!newPass.equals(confirmPass)) {
            etConfirmPass.setError(getString(R.string.error_password_match)); // SỬA LỖI
            isValid = false;
        }

        return isValid;
    }

    /**
     * Chức năng gọi API đổi mật khẩu thực tế
     */
    private void performPasswordChange() {
        // TODO: THAY THẾ code giả lập này bằng logic gọi Firebase/Server để đổi mật khẩu.

        // Giả lập thành công
        Toast.makeText(getContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();

        // Sau khi đổi thành công, quay lại màn hình trước
        NavHostFragment.findNavController(this).popBackStack();
    }
}