package com.example.expense_tracker_app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.TxType;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DebtSummaryAdapter extends RecyclerView.Adapter<DebtSummaryAdapter.VH> {

    public interface OnItemClick {
        void onClick(TxType group); // BORROW hoặc LEND
    }

    private final List<Transaction> data = new ArrayList<>();
    private final OnItemClick listener;

    public DebtSummaryAdapter(OnItemClick listener) {
        this.listener = listener;
    }

    public void setData(List<Transaction> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Transaction t = data.get(pos);

        // Quy ước:
        // - item BORROW: t.type = BORROW, t.amount = y (tổng vay), t.note = x (tổng đã trả)
        // - item LEND  : t.type = LEND,  t.amount = y (tổng cho vay), t.note = x (tổng đã thu)
        long y = Math.abs(t.amount);
        long x = parseLongSafe(t.note);

        if (t.type == TxType.BORROW) {
            h.ivIcon.setImageResource(R.drawable.ic_cat_debt_return);
            h.tvName.setText("ĐI VAY");
            h.tvCategories.setText("Đi vay - Đã trả");
            h.tvInfo.setText("Đã trả " + vnd(x) + " / " + vnd(y));
            h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(TxType.BORROW); });
        } else { // LEND
            h.ivIcon.setImageResource(R.drawable.ic_cat_lend);
            h.tvName.setText("CHO VAY");
            h.tvCategories.setText("Cho vay - Đã thu hồi");
            h.tvInfo.setText("Đã thu hồi " + vnd(x) + " / " + vnd(y));
            h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(TxType.LEND); });
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(2, data.size());
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName, tvCategories, tvInfo;

        VH(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvName = itemView.findViewById(R.id.tvBudgetName);
            tvCategories = itemView.findViewById(R.id.tvCategories);
            tvInfo = itemView.findViewById(R.id.tvSpentInfo);
        }
    }

    private static long parseLongSafe(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return 0L; }
    }

    private static String vnd(long v) {
        NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
        return f.format(v) + " đ";
    }
}
