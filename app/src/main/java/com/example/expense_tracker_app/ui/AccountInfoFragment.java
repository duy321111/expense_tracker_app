package com.example.expense_tracker_app.ui;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.User;
import com.example.expense_tracker_app.data.repository.UserRepository;
import com.example.expense_tracker_app.viewmodel.ProfileViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.Executors;

public class AccountInfoFragment extends AppCompatActivity {

    private ProfileViewModel viewModel;
    private User currentUser; // Biến lưu user hiện tại để cập nhật
    private int userId = 1; // ID mặc định nếu không tìm thấy user

    // Views
    private ImageView btnBack;
    private ImageView imgAvatar;
    private TextView btnChangeAvatar;
    private EditText etName, etEmail;
    private Button btnChangePassword, btnSave;

    private String tempImagePath = ""; // Đường dẫn ảnh tạm thời khi vừa chọn xong (chưa lưu)

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_account_info);

        initViews();
        setupViewModel(); // Hàm load dữ liệu
        registerImagePicker();
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

    // --- 1. LOAD DỮ LIỆU USER TỪ DB ---
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        UserRepository repository = new UserRepository(getApplication());

        // Lấy User đang đăng nhập từ SharedPreferences (hoặc nguồn lưu trữ tạm)
        // Lưu ý: repository.getLoggedInUser() chạy trên main thread nếu là SharedPreferences,
        // nếu nó truy vấn DB thì cần đưa vào background thread.
        // Ở đây giả định bạn lấy được ID user hiện tại.
        User loggedInUser = repository.getLoggedInUser();
        if (loggedInUser != null) {
            userId = loggedInUser.id;
        }

        // Quan sát dữ liệu LiveData từ DB theo ID
        viewModel.getUser(userId).observe(this, user -> {
            if (user != null) {
                currentUser = user; // Lưu lại object user gốc để update sau này

                // Hiển thị thông tin lên giao diện
                etName.setText(user.fullName);
                etEmail.setText(user.email);

                // Chỉ load ảnh từ DB nếu người dùng chưa chọn ảnh mới (tempImagePath rỗng)
                if (tempImagePath.isEmpty() && user.profileImagePath != null && !user.profileImagePath.isEmpty()) {
                    try {
                        imgAvatar.setImageURI(Uri.parse(user.profileImagePath));
                    } catch (Exception e) {
                        e.printStackTrace();
                        imgAvatar.setImageResource(R.drawable.user); // Ảnh mặc định nếu lỗi
                    }
                }
            }
        });
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());

        // Đổi mật khẩu
        btnChangePassword.setOnClickListener(v -> {
            // Chuyển sang Fragment/Activity đổi mật khẩu
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new ChangePasswordFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Click chọn ảnh
        btnChangeAvatar.setOnClickListener(v -> showImagePickerDialog());
        imgAvatar.setOnClickListener(v -> showImagePickerDialog());

        // --- 2. XỬ LÝ NÚT LƯU ---
        btnSave.setOnClickListener(v -> {
            if (currentUser == null) return;

            String newName = etName.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(this, "Họ tên không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật thông tin vào object currentUser
            currentUser.fullName = newName;
            currentUser.email = newEmail;

            // Nếu có chọn ảnh mới thì cập nhật đường dẫn ảnh
            if (!tempImagePath.isEmpty()) {
                currentUser.profileImagePath = tempImagePath;
            }

            // Gọi ViewModel update xuống Database
            viewModel.updateUserInfo(currentUser);

            Toast.makeText(this, "Đã cập nhật thông tin!", Toast.LENGTH_SHORT).show();
            finish(); // Đóng màn hình, quay về ProfileFragment
        });
    }

    private void showImagePickerDialog() {
        String[] options = {"Chọn từ thư viện"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thay đổi ảnh đại diện");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                pickImageLauncher.launch("image/*");
            }
        });
        builder.show();
    }

    private void registerImagePicker() {
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                String internalPath = saveImageToInternalStorage(uri);
                if (internalPath != null) {
                    tempImagePath = internalPath; // Lưu đường dẫn ảnh mới vào biến tạm
                    imgAvatar.setImageURI(Uri.parse(internalPath)); // Hiển thị ngay lên UI
                } else {
                    Toast.makeText(this, "Lỗi lưu ảnh", Toast.LENGTH_SHORT).show();
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