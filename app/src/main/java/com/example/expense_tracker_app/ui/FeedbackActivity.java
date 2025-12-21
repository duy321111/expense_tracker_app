package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker_app.R;

public class FeedbackActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etSubject, etContent;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Ánh xạ View
        btnBack = findViewById(R.id.btn_back);
        etSubject = findViewById(R.id.et_subject);
        etContent = findViewById(R.id.et_content);
        btnSend = findViewById(R.id.btn_send_feedback);

        // Xử lý sự kiện
        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> sendEmail());
    }

    private void sendEmail() {
        String userSubject = etSubject.getText().toString().trim();
        String userContent = etContent.getText().toString().trim();

        if (userSubject.isEmpty() || userContent.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
            return;
        }

        // Email người nhận (Của bạn)
        String[] recipients = {"hotro.expensetracker@gmail.com"};

        // Tự động thêm thông tin thiết bị vào cuối nội dung
        String finalContent = userContent + "\n\n" +
                "----------------------------\n" +
                "Thông tin thiết bị (Vui lòng giữ nguyên):\n" +
                "Device: " + getDeviceName() + "\n" +
                "Android OS: " + Build.VERSION.RELEASE;

        // Tạo Intent chuyển sang ứng dụng Mail
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // Chỉ mở ứng dụng mail
        intent.putExtra(Intent.EXTRA_EMAIL, recipients);
        intent.putExtra(Intent.EXTRA_SUBJECT, "[Góp ý] " + userSubject); // Thêm tiền tố để dễ lọc mail
        intent.putExtra(Intent.EXTRA_TEXT, finalContent);

        try {
            startActivity(Intent.createChooser(intent, "Gửi qua:"));
            // Sau khi mở Gmail xong thì đóng màn hình này lại hoặc giữ nguyên tùy bạn
            // finish();
        } catch (Exception e) {
            Toast.makeText(this, "Không tìm thấy ứng dụng Email nào trên máy!", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm lấy tên máy
    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) return "";
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) return s;
        return Character.toUpperCase(first) + s.substring(1);
    }
}