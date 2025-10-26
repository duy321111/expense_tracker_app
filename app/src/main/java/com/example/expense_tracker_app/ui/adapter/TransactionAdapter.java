package com.example.expense_tracker_app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.TransactionItem;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final List<TransactionItem> transactionList;
    private final Context context;

    public TransactionAdapter(Context context, List<TransactionItem> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionItem item = transactionList.get(position);

        // Gán dữ liệu vào Views (ánh xạ từ item_transaction.xml)
        holder.categoryName.setText(item.getCategoryName());
        holder.paymentMethod.setText(item.getPaymentMethod());
        holder.amount.setText(item.getAmount());
        holder.categoryIcon.setImageResource(item.getIconResId());

        // Tô màu cho số tiền (Accent 3 cho chi tiêu, Success 1 cho thu nhập)
        int colorResId = item.isExpense() ? R.color.accent_3 : R.color.success_1;
        holder.amount.setTextColor(ContextCompat.getColor(context, colorResId));

        // Tùy chỉnh màu nền icon (nếu cần)
        // int iconBgResId = item.isExpense() ? R.drawable.bg_circle_orange : R.drawable.bg_circle_green;
        // holder.categoryIcon.setBackgroundResource(iconBgResId);

        // Tùy chỉnh màu Tint cho icon
        int iconTintColor = item.isExpense() ? R.color.accent_1 : R.color.success_2;
        holder.categoryIcon.setColorFilter(ContextCompat.getColor(context, iconTintColor));
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    // Class ViewHolder
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName, paymentMethod, amount;
        ImageView categoryIcon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ID từ item_transaction.xml
            categoryName = itemView.findViewById(R.id.tv_category_name);
            paymentMethod = itemView.findViewById(R.id.tv_payment_method);
            amount = itemView.findViewById(R.id.tv_amount);
            categoryIcon = itemView.findViewById(R.id.img_category_icon);
        }
    }
}