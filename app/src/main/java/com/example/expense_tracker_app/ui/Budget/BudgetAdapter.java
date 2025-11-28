package com.example.expense_tracker_app.ui.Budget;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Budget;
import com.example.expense_tracker_app.ui.Budget.BudgetDetail;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {
    private final List<Budget> budgets;
    private final Map<String, Integer> categoryIconMap = new HashMap<>();
    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public BudgetAdapter(List<Budget> budgets) {
        this.budgets = budgets;
        initCategoryIconMap();
    }
    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgets.get(position);

        // --- bind text & progress ---
        holder.tvBudgetName.setText(budget.getName());

        if (budget.getCategories() != null && !budget.getCategories().isEmpty()) {
            holder.tvCategories.setText(String.join(", ", budget.getCategories()));
            Integer icon = categoryIconMap.get(budget.getCategories().get(0));
            holder.ivIcon.setImageResource(icon != null ? icon : R.drawable.ic_cat_food);
        } else {
            holder.tvCategories.setText("Không có danh mục");
            holder.ivIcon.setImageResource(R.drawable.ic_cat_food);
        }

        String spentInfo = holder.itemView.getContext().getString(
                R.string.budget_spent_pattern,
                formatCurrency(budget.getSpentAmount()),
                formatCurrency(budget.getAmount())
        );
        holder.tvSpentInfo.setText(spentInfo);

        int progress = budget.getAmount() <= 0 ? 0 :
                (int) Math.min(100, Math.round((budget.getSpentAmount() / budget.getAmount()) * 100));
        holder.pbProgress.setProgress(progress);

        // --- click: open detail screen (BudgetDetail dùng layout budget_detail.xml) ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), BudgetDetail.class);
            intent.putExtra("budget_id", budget.getId());
            intent.putExtra("budget_name", budget.getName());
            intent.putExtra("budget_limit", budget.getAmount());
            intent.putExtra("budget_spent", budget.getSpentAmount());
            intent.putExtra("budget_month", budget.getMonth());
            intent.putExtra("budget_year", budget.getYear());
            v.getContext().startActivity(intent);
        });
    }
    @Override
    public int getItemCount() { return budgets.size(); }
    private void initCategoryIconMap() {
        categoryIconMap.put("Ăn uống", R.drawable.ic_cat_food);
        categoryIconMap.put("Cà phê", R.drawable.ic_cat_coffee);
        categoryIconMap.put("Đi chợ / Siêu thị", R.drawable.ic_cat_groceries);
        categoryIconMap.put("Điện", R.drawable.ic_cat_electric);
        categoryIconMap.put("Nước", R.drawable.ic_cat_water);
        categoryIconMap.put("Internet", R.drawable.ic_cat_internet);
        categoryIconMap.put("Di chuyển", R.drawable.ic_cat_transport);
        categoryIconMap.put("GAS", R.drawable.ic_cat_gas);
    }

    private String formatCurrency(double value) {
        return currencyFormat.format(value).replace("₫", "đ");
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvBudgetName, tvCategories, tvSpentInfo;
        ImageView ivIcon;
        android.widget.ProgressBar pbProgress;
        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBudgetName = itemView.findViewById(R.id.tvBudgetName);
            tvCategories = itemView.findViewById(R.id.tvCategories);
            tvSpentInfo = itemView.findViewById(R.id.tvSpentInfo);
            ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
            pbProgress = itemView.findViewById(R.id.pbProgress);
        }
    }
}
