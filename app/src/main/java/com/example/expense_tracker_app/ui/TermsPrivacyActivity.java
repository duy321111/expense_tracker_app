package com.example.expense_tracker_app.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expense_tracker_app.R;
import com.google.android.material.appbar.MaterialToolbar;

public class TermsPrivacyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_privacy);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Đã sửa: Không cần Html.fromHtml nữa vì dùng text thuần
        // Layout XML đã tự động lấy text từ @string/terms_privacy_content_plain rồi
        // Nhưng nếu muốn set bằng code thì dùng dòng dưới:

        // TextView tvContent = findViewById(R.id.tv_content);
        // tvContent.setText(R.string.terms_privacy_content_plain);
    }
}