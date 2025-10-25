package com.example.expense_tracker_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.TxType;
import com.example.expense_tracker_app.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TxViewHolder> {
    private final List<Transaction> data = new ArrayList<>();
    private final Context context;

    public TransactionAdapter(Context context) {
        this.context = context;
    }

    public void submit(List<Transaction> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull @Override public TxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TxViewHolder(v);
    }

    @Override public void onBindViewHolder(@NonNull TxViewHolder h, int pos) {
        Transaction tx = data.get(pos);

        // Cập nhật thông tin giao dịch
        h.tvDate.setText(String.valueOf(tx.date.getDayOfMonth()));
        h.tvCat.setText(tx.category.name);
        h.tvMethod.setText(tx.method);

        String prefix = (tx.type == TxType.INCOME) ? "+" : "-";
        h.tvAmount.setText(prefix + CurrencyUtils.vnd(tx.amount).trim());

        // Dùng success_1 (xanh) cho INCOME, accent_1 (cam) cho EXPENSE
        int color = (tx.type == TxType.INCOME)
                ? context.getColor(R.color.success_1)
                : context.getColor(R.color.accent_1);
        h.tvAmount.setTextColor(color);

        // Hiển thị Header ngày
        if (pos > 0 && data.get(pos - 1).date.equals(tx.date)) {
            h.tvDateHeader.setVisibility(View.GONE);
        } else {
            h.tvDateHeader.setVisibility(View.VISIBLE);
            h.tvDateHeader.setText("Ngày " + tx.date.getDayOfMonth() + " tháng " + tx.date.getMonthValue() + " " + tx.date.getYear());
        }
    }

    @Override public int getItemCount() { return data.size(); }

    static class TxViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDateHeader;
        final TextView tvDate;
        final TextView tvCat;
        final TextView tvMethod;
        final TextView tvAmount;

        TxViewHolder(View v) {
            super(v);
            tvDateHeader = v.findViewById(R.id.tvDateHeader);
            tvDate = v.findViewById(R.id.tvDate);
            tvCat = v.findViewById(R.id.tvCat);
            tvMethod = v.findViewById(R.id.tvMethod);
            tvAmount = v.findViewById(R.id.tvAmount);
        }
    }
}