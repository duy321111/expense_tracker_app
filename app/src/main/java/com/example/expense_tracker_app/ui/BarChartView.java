package com.example.expense_tracker_app.ui;

import com.example.expense_tracker_app.R;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import java.util.*;

public class BarChartView extends View {
    public static class Bar { public float income, expense; public String label;
        public Bar(String label, float income, float expense){ this.label=label; this.income=income; this.expense=expense; } }
    private final List<Bar> data = new ArrayList<>();
    private final Paint pIncome = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pExpense = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pAxis = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pLabel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float LBL_SPACE = 30f;

    public BarChartView(Context c, AttributeSet a){
        super(c,a);
        // Lấy màu từ R.color
        pIncome.setColor(androidx.core.content.ContextCompat.getColor(c, R.color.success_1));
        pExpense.setColor(androidx.core.content.ContextCompat.getColor(c, R.color.accent_1));
        pAxis.setColor(androidx.core.content.ContextCompat.getColor(c, R.color.neutral_200));
        pLabel.setColor(androidx.core.content.ContextCompat.getColor(c, R.color.neutral_600));
        // ...
    }
    public void setData(List<Bar> bars){ data.clear(); data.addAll(bars); invalidate(); }

    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        if(data.isEmpty()) return;
        float w = getWidth(), h = getHeight(), padding=40f, gap=30f;
        float left = padding, right = w - padding, bottom = h - LBL_SPACE, top = padding;

        c.drawLine(left, bottom, right, bottom, pAxis);

        float max = 0f;
        for(Bar b: data) max = Math.max(max, Math.max(b.income, b.expense));
        if(max==0) max=1;

        float slot = (right - left) / data.size();
        float barW = (slot - gap) / 2f;
        float barPadding = 6f;

        for(int i=0;i<data.size();i++){
            Bar b = data.get(i);
            float x0 = left + i*slot + gap/2f;
            float hIncome = (b.income/max)*(bottom-top);
            float hExpense= (b.expense/max)*(bottom-top);

            rect.set(x0, bottom-hIncome, x0+barW, bottom);
            c.drawRoundRect(rect, 8,8, pIncome);

            rect.set(x0+barW+barPadding, bottom-hExpense, x0+2*barW+barPadding, bottom);
            c.drawRoundRect(rect, 8,8, pExpense);

            c.drawText(b.label, x0 + barW + barPadding/2f, bottom + LBL_SPACE * 0.75f, pLabel);
        }
    }
}