package com.example.expense_tracker_app.ui.Loan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.DailyLoanSection;
import com.example.expense_tracker_app.data.model.LoanTransaction;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DailySectionAdapter extends RecyclerView.Adapter<DailySectionAdapter.SectionVH> {

    public interface OnTransactionClickListener { void onTransactionClick(LoanTransaction t); }

    private final List<DailyLoanSection> sections = new ArrayList<>();
    private final SimpleDateFormat dateFmt =
            new SimpleDateFormat("'Ngày' dd 'tháng' MM yyyy", new Locale("vi", "VN"));
    private final OnTransactionClickListener listener;

    public DailySectionAdapter(OnTransactionClickListener l){ this.listener = l; }

    public void setSections(List<DailyLoanSection> data){
        sections.clear();
        if (data != null) sections.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public SectionVH onCreateViewHolder(@NonNull ViewGroup p, int vt){
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_daily_section, p, false); // tool:listitem=item_daily_section
        return new SectionVH(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionVH h, int pos){
        h.bind(sections.get(pos), dateFmt);
    }

    @Override
    public int getItemCount(){ return sections.size(); }

    // -------- Section ViewHolder (mỗi NGÀY) --------
    static class SectionVH extends RecyclerView.ViewHolder {
        private final TextView tvDate;
        private final RecyclerView rvInSection;
        private final LoanInSectionAdapter childAdapter;

        SectionVH(@NonNull View v, OnTransactionClickListener l){
            super(v);
            tvDate = v.findViewById(R.id.tvSectionDate);
            rvInSection = v.findViewById(R.id.rvSectionTransactions);
            rvInSection.setLayoutManager(new LinearLayoutManager(v.getContext()));
            rvInSection.setNestedScrollingEnabled(false);
            childAdapter = new LoanInSectionAdapter(l);
            rvInSection.setAdapter(childAdapter);
        }

        void bind(DailyLoanSection s, SimpleDateFormat df){
            tvDate.setText(df.format(s.getDate()));
            childAdapter.submit(s.getTransactions());
        }
    }

    // -------- Adapter con cho item trong SECTION --------
    static class LoanInSectionAdapter extends RecyclerView.Adapter<LoanInSectionAdapter.ItemVH> {

        private final List<LoanTransaction> items = new ArrayList<>();
        private final DecimalFormat moneyFmt = new DecimalFormat("#,###");
        private final OnTransactionClickListener listener;

        LoanInSectionAdapter(OnTransactionClickListener l){ this.listener = l; }

        void submit(List<LoanTransaction> data){
            items.clear();
            if (data != null) items.addAll(data);
            notifyDataSetChanged();
        }

        @NonNull @Override
        public ItemVH onCreateViewHolder(@NonNull ViewGroup p, int vt){
            View v = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_loan_transaction_section, p, false);
            return new ItemVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemVH h, int pos){
            LoanTransaction t = items.get(pos);
            h.tvType.setText(t.getTypeDisplay());
            h.tvPerson.setText(t.getPersonName());
            h.tvAmount.setText(moneyFmt.format(t.getAmount()) + " đ");
            // icon nếu cần: h.ivIcon.setImageResource(R.drawable.ic_loan);
            h.itemView.setOnClickListener(v -> { if (listener != null) listener.onTransactionClick(t); });
        }

        @Override
        public int getItemCount(){ return items.size(); }

        static class ItemVH extends RecyclerView.ViewHolder{
            final ImageView ivIcon;
            final TextView tvType, tvPerson, tvAmount;
            ItemVH(@NonNull View v){
                super(v);
                ivIcon  = v.findViewById(R.id.ivTransactionIcon);
                tvType  = v.findViewById(R.id.tvTransactionType);
                tvPerson= v.findViewById(R.id.tvPersonName);
                tvAmount= v.findViewById(R.id.tvAmount);
            }
        }
    }
}
