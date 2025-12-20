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

import java.io.InputStream; // Import thêm
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

        // --- THÔNG TIN CƠ BẢN ---
        h.tvDate.setText(String.format("%02d", tx.date.getDayOfMonth()));
        h.tvCat.setText(tx.category.name);
        h.tvMethod.setText(tx.method);

        String prefix = (tx.type == TxType.INCOME) ? "+" : "-";
        try { h.tvAmount.setText(prefix + CurrencyUtils.vnd(tx.amount)); }
        catch (Exception e) { h.tvAmount.setText(prefix + tx.amount); }

        int colorRes = (tx.type == TxType.INCOME) ? R.color.success_1 : R.color.accent_1;
        h.tvAmount.setTextColor(context.getResources().getColor(colorRes, null));

        if (tx.category.icon != null) {
            int resId = context.getResources().getIdentifier(tx.category.icon, "drawable", context.getPackageName());
            if (resId != 0) h.ivCatIcon.setImageResource(resId);
            else h.ivCatIcon.setImageResource(R.drawable.ic_category);
        }

        // --- HIỂN THỊ CHI TIẾT ---

        // 1. Ghi chú
        if (tx.note != null && !tx.note.trim().isEmpty()) {
            h.tvNote.setVisibility(View.VISIBLE);
            h.tvNote.setText(tx.note);
        } else {
            h.tvNote.setVisibility(View.GONE);
        }

        // 2. Địa điểm
        if (tx.location != null && !tx.location.trim().isEmpty()) {
            h.tvLocation.setVisibility(View.VISIBLE);
            h.tvLocation.setText("📍 " + tx.location);
        } else {
            h.tvLocation.setVisibility(View.GONE);
        }

        // 3. Ảnh
        if (tx.imagePath != null && !tx.imagePath.trim().isEmpty()) {
            h.tvImageLink.setVisibility(View.VISIBLE);
            h.tvImageLink.setText("Ảnh đính kèm");
            h.tvImageLink.setPaintFlags(h.tvImageLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            h.tvImageLink.setOnClickListener(v -> showImagePopup(tx.imagePath));
        } else {
            h.tvImageLink.setVisibility(View.GONE);
        }

        // Header ngày
        if (pos > 0 && data.get(pos - 1).date.isEqual(tx.date)) {
            h.tvDateHeader.setVisibility(View.GONE);
        } else {
            h.tvDateHeader.setVisibility(View.VISIBLE);
            h.tvDateHeader.setText("Ngày " + tx.date.getDayOfMonth() + " tháng " + tx.date.getMonthValue() + " " + tx.date.getYear());
        }
    }

    // --- SỬA LỖI CRASH Ở ĐÂY ---
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

            // Bước 1: Thử mở luồng đọc file để kiểm tra quyền
            // Nếu không có quyền, dòng này sẽ ném SecurityException ngay lập tức
            InputStream inputStream = context.getContentResolver().openInputStream(uri);

            // Bước 2: Nếu mở được, giải mã thành Bitmap và hiển thị
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ivFull.setImageBitmap(bitmap);

            if (inputStream != null) inputStream.close();

        } catch (SecurityException e) {
            // Bắt lỗi quyền truy cập (Ảnh cũ/Lỗi permission)
            e.printStackTrace();
            Toast.makeText(context, "Không thể mở ảnh cũ (Mất quyền truy cập)", Toast.LENGTH_SHORT).show();
            ivFull.setImageResource(R.drawable.ic_image); // Hiển thị ảnh thay thế
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi tải ảnh", Toast.LENGTH_SHORT).show();
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