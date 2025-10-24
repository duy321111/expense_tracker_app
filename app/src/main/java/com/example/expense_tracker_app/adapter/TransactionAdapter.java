package com.example.expense_tracker_app.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense_tracker_app.databinding.ItemTransactionBinding;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.TxType;
import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.VH> {
    private final List<Transaction> data = new ArrayList<>();
    public void submit(List<Transaction> list){ data.clear(); data.addAll(list); notifyDataSetChanged(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemTransactionBinding b;
        VH(ItemTransactionBinding b){ super(b.getRoot()); this.b=b; }
    }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v){
        return new VH(ItemTransactionBinding.inflate(LayoutInflater.from(p.getContext()), p, false));
    }
    @Override public void onBindViewHolder(@NonNull VH h, int i){
        Transaction t = data.get(i);
        h.b.tvCat.setText(t.category.name);
        h.b.tvMethod.setText(t.method);
        String sign = t.type== TxType.INCOME? "+": "-";
        h.b.tvAmount.setText(sign + t.amount + " đ");
    }
    @Override public int getItemCount(){ return data.size(); }
}
