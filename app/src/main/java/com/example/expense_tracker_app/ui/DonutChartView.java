package com.example.expense_tracker_app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class DonutChartView extends View {

    // dữ liệu mẫu (tỉ lệ)
    private float[] values = new float[]{40f, 25f, 20f, 15f}; // tổng = 100
    private final int[] colors = new int[]{
            0xFF7C4DFF, // tím
            0xFFFFB74D, // cam nhạt
            0xFF4DB6AC, // teal
            0xFFE57373  // đỏ nhạt
    };

    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();

    public DonutChartView(Context c) { super(c); init(); }
    public DonutChartView(Context c, @Nullable AttributeSet a) { super(c, a); init(); }
    public DonutChartView(Context c, @Nullable AttributeSet a, int s) { super(c, a, s); init(); }

    private void init() {
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.BUTT);

        bgRingPaint.setStyle(Paint.Style.STROKE);
        bgRingPaint.setColor(0xFFEDEDF1); // neutral_100~150 nền vòng tròn
        bgRingPaint.setStrokeCap(Paint.Cap.BUTT);
    }

    // API đơn giản nếu muốn thay dữ liệu sau
    public void setValues(float[] v) {
        if (v == null || v.length == 0) return;
        this.values = v;
        invalidate();
    }

    public void setColors(int[] cs){ /* lưu cs và invalidate() */ }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float size = Math.min(getWidth(), getHeight());
        float stroke = size * 0.25f;              // bề dày vòng
        float pad = stroke / 2f + size * 0.04f;   // chừa mép
        arcPaint.setStrokeWidth(stroke);
        bgRingPaint.setStrokeWidth(stroke);

        arcRect.set(pad, pad, getWidth() - pad, getHeight() - pad);

        // vòng nền
        canvas.drawArc(arcRect, 0, 360, false, bgRingPaint);

        // tổng giá trị
        float sum = 0f;
        for (float v : values) sum += v;
        if (sum <= 0f) return;

        // vẽ các cung theo tỉ lệ, bắt đầu từ -90° để ở đỉnh
        float start = -90f;
        for (int i = 0; i < values.length; i++) {
            float sweep = (values[i] / sum) * 360f;
            arcPaint.setColor(colors[i % colors.length]);
            canvas.drawArc(arcRect, start, sweep, false, arcPaint);
            start += sweep;
        }
    }
}
