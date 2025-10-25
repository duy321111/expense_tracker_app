package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AnimationSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker_app.R;

public class MainActivity extends AppCompatActivity {

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
