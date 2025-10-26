package com.example.expense_tracker_app.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction; // Import class này
import com.example.expense_tracker_app.R;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gắn layout "cái khung" mà bạn vừa tạo ở Bước 1
        setContentView(R.layout.activity_profile);

        // Chỉ tải Fragment khi Activity được tạo lần đầu
        if (savedInstanceState == null) {

            // 1. Tạo một đối tượng ProfileFragment
            ProfileFragment profileFragment = new ProfileFragment();

            // 2. Lấy trình quản lý Fragment và bắt đầu giao dịch
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // 3. Thay thế nội dung của container (cái FrameLayout) bằng Fragment của bạn
            transaction.replace(R.id.fragment_container_profile, profileFragment);

            // 4. Hoàn tất giao dịch
            transaction.commit();
        }
    }
}