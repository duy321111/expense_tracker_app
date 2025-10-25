package com.example.expense_tracker_app.ui.Loan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.model.DailyLoanSection;
import com.example.expense_tracker_app.model.LoanTransaction;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DailySectionAdapter extends RecyclerView.Adapter<DailySectionAdapter.ViewHolder> {

    private List<DailyLoanSection> sections;
    private SimpleDateFormat dateFormat;
    private DecimalFormat currencyFormat;
    private OnTransactionClickListener listener;

    public interface OnTransactionClickListener {
        void onTransactionClick(LoanTransaction transaction);
    }

    public DailySectionAdapter(OnTransactionClickListener listener) {
        this.sections = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("'Ngày' dd 'tháng' MM yyyy", new Locale("vi", "VN"));
        this.currencyFormat = new DecimalFormat("#,###");
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_section, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyLoanSection section = sections.get(position);
        holder.bind(section);
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    public void setSections(List<DailyLoanSection> sections) {
        this.sections = sections;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSectionDate;
        private LinearLayout llTransactionsContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionDate = itemView.findViewById(R.id.tvSectionDate);
            llTransactionsContainer = itemView.findViewById(R.id.llTransactionsContainer);
        }

        public void bind(DailyLoanSection section) {
            tvSectionDate.setText(dateFormat.format(section.getDate()));
            llTransactionsContainer.removeAllViews();

            List<LoanTransaction> transactions = section.getTransactions();
            for (int i = 0; i < transactions.size(); i++) {
                LoanTransaction transaction = transactions.get(i);
                View transactionView = createTransactionView(transaction);
                llTransactionsContainer.addView(transactionView);

                // Add divider except for last item
                if (i < transactions.size() - 1) {
                    View divider = new View(itemView.getContext());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 1);
                    divider.setLayoutParams(params);
                    divider.setBackgroundColor(
                            ContextCompat.getColor(itemView.getContext(), R.color.neutral_50));
                    llTransactionsContainer.addView(divider);
                }
            }
        }

        private View createTransactionView(LoanTransaction transaction) {
            View view = LayoutInflater.from(itemView.getContext())
                    .inflate(R.layout.item_loan_transaction, llTransactionsContainer, false);

            // Find views
            CardView cvIconContainer = view.findViewById(R.id.cvIconContainer);
            ImageView ivIcon = view.findViewById(R.id.ivTransactionIcon);
            TextView tvType = view.findViewById(R.id.tvTransactionType);
            TextView tvPerson = view.findViewById(R.id.tvPersonName);
            TextView tvAmount = view.findViewById(R.id.tvAmount);

            // Set icon background color
            cvIconContainer.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(), R.color.success_1));

            // Set transaction data
            tvType.setText(transaction.getTypeDisplay());
            tvPerson.setText(transaction.getPersonName());
            tvAmount.setText(currencyFormat.format(transaction.getAmount()) + " đ");

            // Set click listener
            view.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTransactionClick(transaction);
                }
            });

            return view;
        }
    }
}