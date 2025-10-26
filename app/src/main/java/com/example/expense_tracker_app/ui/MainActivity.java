package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding b;

    @Override protected void onCreate(Bundle s){
        super.onCreate(s);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // Mặc định mở màn Thêm giao dịch
        replace(new AddTransactionFragment());

        b.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // TODO: thay bằng HomeFragment khi có
                replace(new AddTransactionFragment());
                return true;
            }

    private static final int SPLASH_DELAY = 2000; // 2 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView logo = findViewById(R.id.logo);

        // Hiệu ứng bùng nổ: scale + fade-in
        AnimationSet explosion = new AnimationSet(true);

        // Scale từ 0 → 1.5x → 1.0x để tạo cảm giác “bùng nổ”
        ScaleAnimation scale = new ScaleAnimation(
                0f, 1f,  // fromX, toX
                0f, 1f,  // fromY, toY
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f, // pivotX
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f  // pivotY
        );
        scale.setDuration(1000);

        // Alpha từ 0 → 1
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1000);

        // Thêm vào AnimationSet
        explosion.addAnimation(scale);
        explosion.addAnimation(fadeIn);

        // Giữ trạng thái cuối cùng
        explosion.setFillAfter(true);

        // Start animation
        logo.startAnimation(explosion);

        // Delay tổng thể 2 giây rồi chuyển sang HomeActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        }, SPLASH_DELAY);
    }
}
