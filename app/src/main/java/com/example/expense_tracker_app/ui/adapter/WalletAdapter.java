package com.example.expense_tracker_app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Wallet;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.WalletViewHolder> {

    private List<Wallet> list = new ArrayList<>();
    private final OnWalletClickListener listener;

    public interface OnWalletClickListener {
        void onWalletClick(Wallet wallet); // Hàm xử lý khi click vào ví
    }

    public WalletAdapter(OnWalletClickListener listener) {
        this.listener = listener;
    }

    public void setList(List<Wallet> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WalletViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Lưu ý: Bạn cần đảm bảo đã có layout item_wallet.xml (layout cho 1 dòng ví)
        // Nếu chưa có, hãy tạo nó đơn giản gồm 1 ImageView và 2 TextView
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet, parent, false);
        return new WalletViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull WalletViewHolder holder, int position) {
        Wallet w = list.get(position);
        holder.tvName.setText(w.name);

        // Format tiền tệ Việt Nam
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvBalance.setText(formatter.format(w.balance) + " đ");

        // Set Icon dựa trên loại ví (giả sử bạn lưu string icon hoặc check tên)
        if (w.name.toLowerCase().contains("tiền mặt")) {
            holder.imgIcon.setImageResource(R.drawable.ic_wallet); // Hãy đảm bảo có icon này
        } else {
            holder.imgIcon.setImageResource(R.drawable.ic_transfer); // Hãy đảm bảo có icon này
        }

        holder.itemView.setOnClickListener(v -> listener.onWalletClick(w));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class WalletViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBalance;
        ImageView imgIcon;

        public WalletViewHolder(View v) {
            super(v);
            // Sửa ID ở đây cho khớp với file item_wallet.xml của bạn
            tvName = v.findViewById(R.id.tv_wallet_name);
            tvBalance = v.findViewById(R.id.tv_wallet_balance);
            imgIcon = v.findViewById(R.id.img_wallet_icon);
        }
    }
}