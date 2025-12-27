package com.example.expense_tracker_app.ui.Budget; // Hoặc package adapter của bạn

import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.expense_tracker_app.data.model.Subcategory;
import com.example.expense_tracker_app.data.repository.TransactionRepository;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Budget;
import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<Budget> budgetList;
    private int userId;

    // --- PHẦN 1: KHAI BÁO INTERFACE & BIẾN LISTENER (THÊM VÀO ĐÂY) ---
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Budget budget);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    // ------------------------------------------------------------------

    public BudgetAdapter(List<Budget> budgetList, int userId) {
        this.budgetList = budgetList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgetList.get(position);

        holder.tvBudgetName.setText(budget.getName());

        // Hiển thị tên các subcategory
        List<Integer> subIds = budget.getSubcategoryIds();
        StringBuilder subNames = new StringBuilder();
        double totalSpent = 0;
        if (subIds != null && !subIds.isEmpty()) {
            TransactionRepository repo = new TransactionRepository((Application) holder.itemView.getContext().getApplicationContext());
            for (int i = 0; i < subIds.size(); i++) {
                Subcategory sub = repo.findSubcategory(subIds.get(i));
                if (sub != null) {
                    subNames.append(sub.name);
                    if (i < subIds.size() - 1) subNames.append(", ");
                }
            }
            // Tính tổng đã chi cho các subcategory này trong tháng/năm ngân sách (dùng epoch day)
            java.time.LocalDate firstDayOfMonth = java.time.LocalDate.of(budget.getYear(), budget.getMonth(), 1);
            java.time.LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
            long startEpochDay = firstDayOfMonth.toEpochDay();
            long endEpochDay = lastDayOfMonth.toEpochDay();
            totalSpent = repo.getTotalSpentBySubcategories(userId, startEpochDay, endEpochDay, subIds);
        }
        holder.tvCategories.setText(subNames.length() > 0 ? subNames.toString() : "");

        // Hiển thị số tiền đã chi / hạn mức
        String spentStr = String.format("%,.0f đ", totalSpent);
        String limitStr = String.format("%,.0f đ", budget.getAmount());
        String spentInfo = "Đã chi " + spentStr + " / " + limitStr;
        holder.tvSpentInfo.setText(spentInfo);
        // Tô đỏ chỉ phần số tiền đã chi nếu vượt hạn mức, còn số giới hạn giữ nguyên màu mặc định
        if (budget.getAmount() > 0 && totalSpent > budget.getAmount()) {
            // Tô đỏ phần "Đã chi ... đ"
            int start = spentInfo.indexOf(spentStr);
            int end = start + spentStr.length();
            android.text.Spannable spannable = new android.text.SpannableString(spentInfo);
            spannable.setSpan(new android.text.style.ForegroundColorSpan(
                holder.itemView.getContext().getResources().getColor(R.color.error_1)),
                start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tvSpentInfo.setText(spannable);
        } else {
            // Bình thường để xanh lá (success_1)
            int start = spentInfo.indexOf(spentStr);
            int end = start + spentStr.length();
            android.text.Spannable spannable = new android.text.SpannableString(spentInfo);
            spannable.setSpan(new android.text.style.ForegroundColorSpan(
                holder.itemView.getContext().getResources().getColor(R.color.success_1)),
                start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tvSpentInfo.setText(spannable);
        }

        // --- PHẦN 2: GẮN SỰ KIỆN CLICK (THÊM VÀO ĐÂY) ---
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(budget);
            }
        });
        // ------------------------------------------------
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    // ViewHolder class
    public static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvBudgetName, tvCategories, tvSpentInfo;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBudgetName = itemView.findViewById(R.id.tvBudgetName);
            tvCategories = itemView.findViewById(R.id.tvCategories);
            tvSpentInfo = itemView.findViewById(R.id.tvSpentInfo);
        }
    }
}