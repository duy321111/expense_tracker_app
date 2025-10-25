package com.example.expense_tracker_app.ui.Budget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.model.ExpenseDetail;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDetailAdapter extends RecyclerView.Adapter<ExpenseDetailAdapter.ViewHolder> {

    private List<ExpenseDetail> expenseDetails;
    private DecimalFormat decimalFormat;

    public ExpenseDetailAdapter() {
        this.expenseDetails = new ArrayList<>();
        this.decimalFormat = new DecimalFormat("#,###");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExpenseDetail detail = expenseDetails.get(position);
        holder.bind(detail);
    }

    @Override
    public int getItemCount() {
        return expenseDetails.size();
    }

    public void setExpenseDetails(List<ExpenseDetail> details) {
        this.expenseDetails = details;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivExpenseIcon;
        private TextView tvExpenseTitle;
        private TextView tvExpenseAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivExpenseIcon = itemView.findViewById(R.id.ivExpenseIcon);
            tvExpenseTitle = itemView.findViewById(R.id.tvExpenseTitle);
            tvExpenseAmount = itemView.findViewById(R.id.tvExpenseAmount);
        }

        public void bind(ExpenseDetail detail) {
            ivExpenseIcon.setImageResource(detail.getIconRes());
            tvExpenseTitle.setText(detail.getTitle());

            if (detail.getAmount() > 0) {
                String amountText = decimalFormat.format(detail.getAmount()) +
                        " đ" + detail.getFrequency();
                tvExpenseAmount.setText(amountText);
                tvExpenseAmount.setVisibility(View.VISIBLE);
            } else {
                tvExpenseAmount.setVisibility(View.GONE);
            }
        }
    }
}