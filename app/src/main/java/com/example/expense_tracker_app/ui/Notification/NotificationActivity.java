package com.example.expense_tracker_app.ui.Notification;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.type.NotificationType;
import com.example.expense_tracker_app.ui.SpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private LinearLayout llEmptyState;
    private ImageView ivBack;
    private ImageView ivMarkAllRead;
    private ProgressBar progressBar;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadMockData();
    }

    private void initViews() {
        rvNotifications = findViewById(R.id.rvNotifications);
        llEmptyState = findViewById(R.id.llEmptyState);
        ivBack = findViewById(R.id.ivBack);
        ivMarkAllRead = findViewById(R.id.ivMarkAllRead);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(new NotificationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(NotificationItem item) {
                onNotificationClick(item);
            }
        });

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        // Add item decoration for spacing
        rvNotifications.addItemDecoration(new SpacingItemDecoration(8));
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ivMarkAllRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markAllAsRead();
            }
        });
    }

    private void loadMockData() {
        showLoading(true);

        // Simulate network delay
        rvNotifications.postDelayed(new Runnable() {
            @Override
            public void run() {
                List<NotificationItem> mockNotifications = getMockNotifications();
                adapter.setNotificationList(mockNotifications);
                showLoading(false);
                updateEmptyState(mockNotifications.isEmpty());
            }
        }, 1000);
    }

    private List<NotificationItem> getMockNotifications() {
        List<NotificationItem> list = new ArrayList<>();

        list.add(new NotificationItem(
                "1",
                "Chào đồng bạn",
                "Chào mừng bạn đã tới với Ứng dụng Thương mại điện tử của chúng tôi, chúng tôi sẽ mang đến trải nghiệm tốt nhất cho bạn",
                "08:30 - 19/02/2023",
                false,
                NotificationType.GENERAL
        ));

        list.add(new NotificationItem(
                "2",
                "Đơn hàng đã được xác nhận",
                "Đặt đơn hàng thành công và các mặt hàng bạn đã đặt đang được xử lý. Chúng tôi sẽ thông báo cho bạn khi đơn hàng được giao",
                "08:30 - 19/02/2023",
                false,
                NotificationType.ORDER
        ));

        list.add(new NotificationItem(
                "3",
                "Khuyến mãi đặc biệt",
                "Đặt hàng ngay hôm nay với khuyến mãi đặc biệt! Giảm giá lên đến 50% cho tất cả các mặt hàng. Nhanh tay đặt hàng ngay!",
                "08:30 - 19/02/2023",
                false,
                NotificationType.PROMOTION
        ));

        list.add(new NotificationItem(
                "4",
                "Cập nhật hệ thống",
                "Hệ thống sẽ được bảo trì vào lúc 2:00 AM ngày mai. Vui lòng hoàn tất các giao dịch trước thời gian này.",
                "07:15 - 19/02/2023",
                true,
                NotificationType.SYSTEM
        ));

        list.add(new NotificationItem(
                "5",
                "Đơn hàng đang giao",
                "Đơn hàng #12345 của bạn đang trên đường giao đến. Dự kiến giao hàng trong vòng 2 giờ tới.",
                "06:45 - 19/02/2023",
                true,
                NotificationType.ORDER
        ));

        return list;
    }

    private void onNotificationClick(NotificationItem item) {
        Toast.makeText(this, "Đã nhấn: " + item.getTitle(), Toast.LENGTH_SHORT).show();

        // Mark as read
        item.setRead(true);
        adapter.notifyDataSetChanged();

//         TODO1: Navigate to detail screen or perform action
        // Intent intent = new Intent(this, NotificationDetailActivity.class);
        // intent.putExtra("notification_id", item.getId());
        // startActivity(intent);
    }

    private void markAllAsRead() {
        Toast.makeText(this, "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show();
        // TODO1: Implement mark all as read functionality
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvNotifications.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState(boolean isEmpty) {
        llEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvNotifications.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}