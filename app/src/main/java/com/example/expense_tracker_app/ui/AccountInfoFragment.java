package com.example.expense_tracker_app.ui;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity; // Sửa thành Activity
import androidx.lifecycle.ViewModelProvider;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.User;
import com.example.expense_tracker_app.viewmodel.ProfileViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AccountInfoFragment extends AppCompatActivity {

    private ProfileViewModel viewModel;
    private User currentUser;
    private int userId = 1; // ID mặc định

    // Views
    private ImageView btnBack;
    private ImageView imgAvatar;
    private TextView btnChangeAvatar;
    private EditText etName, etEmail;
    private Button btnChangePassword, btnSave;

    private String tempImagePath = "";

    // SỬA 1: Đổi thành String (dành cho GetContent) thay vì String[]
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_account_info);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        initViews();
        registerImagePicker();
        observeUser();
        setupEvents();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        imgAvatar = findViewById(R.id.img_avatar);
        btnChangeAvatar = findViewById(R.id.btn_change_avatar);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnSave = findViewById(R.id.btn_save);
    }

    private void observeUser() {
        viewModel.getUser(userId).observe(this, user -> {
            if (user != null) {
                currentUser = user;

                if (etName.getText().toString().isEmpty())
                    etName.setText(user.fullName);
                if (etEmail.getText().toString().isEmpty())
                    etEmail.setText(user.email);

                if (user.profileImagePath != null && !user.profileImagePath.isEmpty()) {
                    imgAvatar.setImageURI(Uri.parse(user.profileImagePath));
                    tempImagePath = user.profileImagePath;
                } else {
                    imgAvatar.setImageResource(R.drawable.ic_launcher_background);
                }
            }
        });
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());

        // SỬA 2: Gọi launch với chuỗi "image/*" để mở Gallery
        btnChangeAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        imgAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> {
            if (currentUser != null) {
                String newName = etName.getText().toString().trim();
                String newEmail = etEmail.getText().toString().trim();

                if (newName.isEmpty() || newEmail.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                currentUser.fullName = newName;
                currentUser.email = newEmail;
                currentUser.profileImagePath = tempImagePath;

                viewModel.updateUserInfo(currentUser);
                Toast.makeText(this, "Đã lưu thông tin!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.reset_password, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if(dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        View imgLogo = view.findViewById(R.id.imgLogo);
        View imgLang = view.findViewById(R.id.imgLanguage);
        View tvVN = view.findViewById(R.id.VN);
        if(imgLogo != null) imgLogo.setVisibility(View.GONE);
        if(imgLang != null) imgLang.setVisibility(View.GONE);
        if(tvVN != null) tvVN.setVisibility(View.GONE);

        EditText etNewPass = view.findViewById(R.id.edtNewPassword);
        EditText etConfirmPass = view.findViewById(R.id.edtConfirmPassword);
        Button btnConfirm = view.findViewById(R.id.btnResetPassword);
        TextView tvBack = view.findViewById(R.id.tvLoginRedirect);

        if (tvBack != null) tvBack.setText("Hủy bỏ");

        btnConfirm.setOnClickListener(v -> {
            String newPass = etNewPass.getText().toString();
            String confirmPass = etConfirmPass.getText().toString();

            if (newPass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
            } else if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.changePassword(userId, newPass);
                Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        if (tvBack != null) {
            tvBack.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    // SỬA 3: Dùng GetContent (Gallery) thay vì OpenDocument (File Manager)
    private void registerImagePicker() {
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                // Copy ảnh vào bộ nhớ riêng (Vẫn giữ logic này để an toàn)
                String internalPath = saveImageToInternalStorage(uri);
                if (internalPath != null) {
                    tempImagePath = internalPath;
                    imgAvatar.setImageURI(Uri.parse(internalPath));
                }
            }
        });
    }

    private String saveImageToInternalStorage(Uri sourceUri) {
        try {
            String fileName = "avatar_" + System.currentTimeMillis() + ".jpg";
            File file = new File(getFilesDir(), fileName);
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, length);
            outputStream.close();
            inputStream.close();
            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}