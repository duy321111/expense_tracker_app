package com.example.expense_tracker_app.ui;

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
    private final RectF rect = new RectF();

    public BarChartView(Context c, AttributeSet a){ super(c,a);
        pIncome.setColor(Color.parseColor("#00CE21"));
        pExpense.setColor(Color.parseColor("#F46616"));
        pAxis.setColor(Color.parseColor("#CCCCCC")); pAxis.setStrokeWidth(2f);
    }
    public void setData(List<Bar> bars){ data.clear(); data.addAll(bars); invalidate(); }

    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        if(data.isEmpty()) return;
        float w = getWidth(), h = getHeight(), padding=40f, gap=30f;
        float left = padding, right = w - padding, bottom = h - padding, top = padding;
        c.drawLine(left, bottom, right, bottom, pAxis);

        float max = 0f;
        for(Bar b: data) max = Math.max(max, Math.max(b.income, b.expense));
        if(max==0) max=1;

        float slot = (right - left) / data.size();
        float barW = (slot - gap) / 2f;

        for(int i=0;i<data.size();i++){
            Bar b = data.get(i);
            float x0 = left + i*slot + gap/2f;
            float hIncome = (b.income/max)*(bottom-top);
            float hExpense= (b.expense/max)*(bottom-top);

            rect.set(x0, bottom-hIncome, x0+barW, bottom);
            c.drawRoundRect(rect, 8,8, pIncome);

            rect.set(x0+barW+6, bottom-hExpense, x0+2*barW+6, bottom);
            c.drawRoundRect(rect, 8,8, pExpense);
        }
    }
}
