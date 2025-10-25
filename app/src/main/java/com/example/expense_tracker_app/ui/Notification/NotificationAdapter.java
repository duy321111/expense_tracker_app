package com.example.expense_tracker_app.ui.Notification;// NotificationAdapter.java


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.type.NotificationType;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationItem> notificationList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(NotificationItem item);
    }

    public NotificationAdapter(OnItemClickListener listener) {
        this.notificationList = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void setNotificationList(List<NotificationItem> list) {
        this.notificationList = list;
        notifyDataSetChanged();
    }

    public void addNotification(NotificationItem item) {
        notificationList.add(item);
        notifyItemInserted(notificationList.size() - 1);
    }

    public void clearNotifications() {
        notificationList.clear();
        notifyDataSetChanged();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvMessage;
        private TextView tvTime;
        private ImageView ivIcon;
        private View vUnreadIndicator;
        private CardView cvIconContainer;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
            ivIcon = itemView.findViewById(R.id.ivNotificationIcon);
            vUnreadIndicator = itemView.findViewById(R.id.vUnreadIndicator);
            cvIconContainer = itemView.findViewById(R.id.cvIconContainer);
        }

        public void bind(final NotificationItem item, final OnItemClickListener listener) {
            tvTitle.setText(item.getTitle());
            tvMessage.setText(item.getMessage());
            tvTime.setText(item.getTime());

            // Show/hide unread indicator
            vUnreadIndicator.setVisibility(item.isRead() ? View.GONE : View.VISIBLE);

            // Set icon and color based on type
            setIconByType(item.getIconType());

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(item);
                    }
                }
            });
        }

        private void setIconByType(NotificationType type) {
            int iconRes = R.drawable.ic_notification;
            int colorRes = 0xFF2196F3; // Blue

            switch (type) {
                case GENERAL:
                    iconRes = R.drawable.ic_notification;
                    colorRes = 0xFF2196F3; // Blue
                    break;
                case PROMOTION:
                    iconRes = R.drawable.ic_promotion;
                    colorRes = 0xFFFF9800; // Orange
                    break;
                case SYSTEM:
                    iconRes = R.drawable.ic_system;
                    colorRes = 0xFF9C27B0; // Purple
                    break;
                case ORDER:
                    iconRes = R.drawable.ic_order;
                    colorRes = 0xFF4CAF50; // Green
                    break;
            }

            ivIcon.setImageResource(iconRes);
            cvIconContainer.setCardBackgroundColor(colorRes);
        }
    }
}