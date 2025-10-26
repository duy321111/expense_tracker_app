package com.example.expense_tracker_app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense_tracker_app.R;
import java.util.List;

public class TypePickerAdapter extends RecyclerView.Adapter<TypePickerAdapter.VH> {

    public interface OnPicked { void onPicked(TypeItem item, int position); }

    private final List<TypeItem> data;
    private int selected;
    private final OnPicked listener;

    public TypePickerAdapter(List<TypeItem> data, int selected, OnPicked l) {
        this.data = data;
        this.selected = Math.max(0, Math.min(selected, data.size() - 1));
        this.listener = l;
    }

    public int getSelected() { return selected; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int vType) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_type, p, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        TypeItem it = data.get(pos);

        h.tv.setText(it.name);
        h.icon.setImageResource(it.iconRes);
        h.iconBg.setBackgroundResource(it.roundBgRes);

        boolean isSel = pos == selected;
        h.itemView.setBackgroundResource(
                isSel ? R.drawable.bg_card_primary_5 : R.drawable.bg_card_neutral_50
        );
        h.tv.setTextColor(h.itemView.getResources().getColor(
                isSel ? R.color.primary_1 : R.color.neutral_900
        ));

        h.itemView.setOnClickListener(v -> {
            int old = selected;
            selected = h.getBindingAdapterPosition();
            notifyItemChanged(old);
            notifyItemChanged(selected);
            if (listener != null) listener.onPicked(it, selected);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tv;
        final ImageView icon;
        final View iconBg;

        VH(View v){
            super(v);
            tv = v.findViewById(R.id.tvName);
            icon = v.findViewById(R.id.icon);
            iconBg = v.findViewById(R.id.iconBg);
        }
    }

    // model
    public static class TypeItem {
        public final String name;
        public final int iconRes;
        public final int roundBgRes;
        public TypeItem(String n, int i, int bg){ name = n; iconRes = i; roundBgRes = bg; }
    }
}
