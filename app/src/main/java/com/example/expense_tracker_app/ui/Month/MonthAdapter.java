package com.example.expense_tracker_app.ui.Month;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.google.android.material.chip.Chip;
import java.util.List;


public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.VH> {
    private final List<MonthItem> items;
    public int selected = -1;
    public interface OnMonthSelectedListener {
        void onMonthSelected(int position, MonthItem item);
    }
    private OnMonthSelectedListener listener;

    public MonthAdapter(List<MonthItem> items){ this.items = items; }
    public void setOnMonthSelectedListener(OnMonthSelectedListener l) { this.listener = l; }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt){
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_month, p, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos){
        MonthItem it = items.get(pos);
        h.tvYear.setText(String.valueOf(it.year));
        h.chip.setText("Tháng " + it.month);

        // ngắt listener cũ do recycle
        h.chip.setOnCheckedChangeListener(null);
        h.chip.setChecked(pos == selected);

        // chỉ cho phép 1 mục được chọn
        h.chip.setOnCheckedChangeListener((button, isChecked) -> {
            if (!isChecked) {
                // không cho bỏ chọn nếu là mục đang được chọn
                if (pos == selected) h.chip.setChecked(true);
                return;
            }
            int prev = selected;
            selected = pos;
            if (prev != -1 && prev != pos) notifyItemChanged(prev);
            notifyItemChanged(pos);
            if (listener != null) listener.onMonthSelected(pos, it);
        });

        // click cả item cũng chọn chip
        h.itemView.setOnClickListener(v -> h.chip.setChecked(true));
    }

    @Override public int getItemCount(){ return items.size(); }

    static class VH extends RecyclerView.ViewHolder{
        final TextView tvYear; final Chip chip;
        VH(@NonNull View v){ super(v); tvYear=v.findViewById(R.id.tvYear); chip=v.findViewById(R.id.chipMonth); }
    }
}

