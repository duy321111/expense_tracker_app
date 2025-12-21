package com.example.expense_tracker_app.ui.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.TxType;
import com.example.expense_tracker_app.utils.CurrencyUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TxViewHolder> {
    private final List<Transaction> data = new ArrayList<>();
    private final Context context;

    public TransactionAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<Transaction> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public TxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_stat, parent, false);
        return new TxViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TxViewHolder h, int pos) {
        Transaction tx = data.get(pos);

        // --- 1. NGÀY THÁNG ---
        h.tvDate.setText(String.format("%02d", tx.date.getDayOfMonth()));

        // --- 2. DANH MỤC (SỬA LỖI TIẾNG ANH Ở ĐÂY) ---
        String catName = tx.category.name;
        // Kiểm tra nếu tên là tiếng Anh (do lưu mặc định) thì hiển thị tiếng Việt
        if (catName != null) {
            switch (catName) {
                case "INCOME": catName = "Thu nhập"; break;
                case "EXPENSE": catName = "Chi tiêu"; break;
                case "BORROW": catName = "Đi vay"; break;
                case "LEND": catName = "Cho vay"; break;
            }
        }
        h.tvCat.setText(catName);

        // --- 3. PHƯƠNG THỨC (VÍ) ---
        h.tvMethod.setText(tx.method);

        // --- 4. SỐ TIỀN & MÀU SẮC ---
        boolean isPositive = (tx.type == TxType.INCOME || tx.type == TxType.BORROW);

        String prefix = isPositive ? "+" : "-";
        int colorRes = isPositive ? R.color.success_1 : R.color.accent_1;

        try {
            h.tvAmount.setText(prefix + CurrencyUtils.vnd(Math.abs(tx.amount)));
        } catch (Exception e) {
            h.tvAmount.setText(prefix + Math.abs(tx.amount));
        }

        h.tvAmount.setTextColor(context.getResources().getColor(colorRes, null));

        // --- 5. ICON ---
        if (tx.category.icon != null) {
            int resId = context.getResources().getIdentifier(tx.category.icon, "drawable", context.getPackageName());
            if (resId != 0) h.ivCatIcon.setImageResource(resId);
            else h.ivCatIcon.setImageResource(R.drawable.ic_category);
        }

        // --- HIỂN THỊ CHI TIẾT ---

        // 6. Ghi chú
        if (tx.note != null && !tx.note.trim().isEmpty()) {
            h.tvNote.setVisibility(View.VISIBLE);
            h.tvNote.setText(tx.note);
        } else {
            h.tvNote.setVisibility(View.GONE);
        }

        // 7. Địa điểm
        if (tx.location != null && !tx.location.trim().isEmpty()) {
            h.tvLocation.setVisibility(View.VISIBLE);
            h.tvLocation.setText("📍 " + tx.location);
        } else {
            h.tvLocation.setVisibility(View.GONE);
        }

        // 8. Ảnh
        if (tx.imagePath != null && !tx.imagePath.trim().isEmpty()) {
            h.tvImageLink.setVisibility(View.VISIBLE);
            h.tvImageLink.setText("Xem ảnh đính kèm");
            h.tvImageLink.setPaintFlags(h.tvImageLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            h.tvImageLink.setOnClickListener(v -> showImagePopup(tx.imagePath));
        } else {
            h.tvImageLink.setVisibility(View.GONE);
        }

        // 9. Header ngày
        if (pos > 0 && data.get(pos - 1).date.isEqual(tx.date)) {
            h.tvDateHeader.setVisibility(View.GONE);
        } else {
            h.tvDateHeader.setVisibility(View.VISIBLE);
            h.tvDateHeader.setText("Ngày " + tx.date.getDayOfMonth() + " tháng " + tx.date.getMonthValue() + " " + tx.date.getYear());
        }
    }

    private void showImagePopup(String imageUriStr) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image_view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView ivFull = dialog.findViewById(R.id.ivFullImage);

        try {
            Uri uri = Uri.parse(imageUriStr);
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ivFull.setImageBitmap(bitmap);
            if (inputStream != null) inputStream.close();
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(context, "Không thể mở ảnh (Mất quyền truy cập file)", Toast.LENGTH_SHORT).show();
            ivFull.setImageResource(R.drawable.ic_image);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi khi tải ảnh", Toast.LENGTH_SHORT).show();
        }

        ivFull.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override public int getItemCount() { return data.size(); }

    static class TxViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateHeader, tvDate, tvCat, tvMethod, tvAmount;
        ImageView ivCatIcon;
        TextView tvNote, tvLocation, tvImageLink;

        TxViewHolder(View v) {
            super(v);
            tvDateHeader = v.findViewById(R.id.tvDateHeader);
            tvDate = v.findViewById(R.id.tvDate);
            tvCat = v.findViewById(R.id.tvCat);
            tvMethod = v.findViewById(R.id.tvMethod);
            tvAmount = v.findViewById(R.id.tvAmount);
            ivCatIcon = v.findViewById(R.id.ivCatIcon);
            tvNote = v.findViewById(R.id.tvNote);
            tvLocation = v.findViewById(R.id.tvLocation);
            tvImageLink = v.findViewById(R.id.tvImageLink);
        }
    }
}