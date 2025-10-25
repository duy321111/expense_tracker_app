package com.example.expense_tracker_app.ui.Budget;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.model.BudgetItem;
import com.example.expense_tracker_app.model.ExpenseDetail;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class BudgetDetailActivity extends AppCompatActivity {

    // Header views
    private ImageView ivBack;
    private ImageView ivEdit;

    // Category card views
    private ImageView ivCategoryIcon;
    private TextView tvCategoryName;
    private TextView tvLimitAmount;
    private ProgressBar progressBar;
    private TextView tvSpentAmount;
    private TextView tvProgressPercentage;

    // Expense details
    private CardView cvExpenseDetails;
    private LinearLayout llExpenseDetailsList;
    private boolean isExpenseDetailsExpanded = false;

    // Budget information views
    private TextView tvActualSpending;
    private TextView tvRecommendedSpending;
    private TextView tvEstimatedSpending;

    // Transfer switch
    private CardView cvTransfer;
    private SwitchCompat switchTransfer;

    // Add budget button
    private Button btnAddBudget;

    // Data
    private BudgetItem currentBudget;
    private List<ExpenseDetail> expenseDetails;

    // Formatter
    private DecimalFormat decimalFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_detail);

        initViews();
        setupFormatters();
        loadMockData();
        setupClickListeners();
        displayBudgetData();
    }

    private void initViews() {
        // Header
        ivBack = findViewById(R.id.ivBack);
        ivEdit = findViewById(R.id.ivEdit);

        // Category card
        ivCategoryIcon = findViewById(R.id.ivCategoryIcon);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvLimitAmount = findViewById(R.id.tvLimitAmount);
        progressBar = findViewById(R.id.progressBar);
        tvSpentAmount = findViewById(R.id.tvSpentAmount);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);

        // Expense details
        cvExpenseDetails = findViewById(R.id.cvExpenseDetails);
        llExpenseDetailsList = findViewById(R.id.llExpenseDetailsList);

        // Budget information
        tvActualSpending = findViewById(R.id.tvActualSpending);
        tvRecommendedSpending = findViewById(R.id.tvRecommendedSpending);
        tvEstimatedSpending = findViewById(R.id.tvEstimatedSpending);

        // Transfer
        cvTransfer = findViewById(R.id.cvTransfer);
        switchTransfer = findViewById(R.id.switchTransfer);

        // Button
        btnAddBudget = findViewById(R.id.btnAddBudget);
    }

    private void setupFormatters() {
        decimalFormat = new DecimalFormat("#,###");
    }

    private void loadMockData() {
        // Load mock budget data
        currentBudget = new BudgetItem(
                "1",
                "Mua sắm",
                12000000,
                7440000,
                420000,
                6458000,
                "monthly"
        );

        // Load mock expense details
        expenseDetails = new ArrayList<>();
        expenseDetails.add(new ExpenseDetail(
                "Chi tiết khoản chi",
                0,
                "",
                R.drawable.ic_list
        ));
        expenseDetails.add(new ExpenseDetail(
                "Thực tế đã chi",
                300000,
                "/ngày",
                R.drawable.ic_wallet
        ));
        expenseDetails.add(new ExpenseDetail(
                "Nên chi",
                420000,
                "/ngày",
                R.drawable.ic_trending_down
        ));
        expenseDetails.add(new ExpenseDetail(
                "Dự kiến chi tiêu",
                6458000,
                "",
                R.drawable.ic_chart
        ));
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(BudgetDetailActivity.this,
                        "Chỉnh sửa hạn mức", Toast.LENGTH_SHORT).show();
                // TODO: Open edit budget screen
            }
        });

        cvExpenseDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleExpenseDetails();
            }
        });

        switchTransfer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String message = isChecked ?
                    "Đã bật chuyển kì tiếp" : "Đã tắt chuyển kì tiếp";
            Toast.makeText(BudgetDetailActivity.this, message, Toast.LENGTH_SHORT).show();
        });

        btnAddBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(BudgetDetailActivity.this,
                        "Thêm hạn mức mới", Toast.LENGTH_SHORT).show();
                // TODO: Open add budget screen
            }
        });
    }

    private void displayBudgetData() {
        if (currentBudget == null) return;

        // Set category name and limit
        tvCategoryName.setText(currentBudget.getCategory());
        tvLimitAmount.setText(formatCurrency(currentBudget.getLimitAmount()));

        // Set progress
        int progress = currentBudget.getProgressPercentage();
        animateProgress(progressBar, 0, progress);

        // Set spent amount
        String spentText = String.format("%s / %s đ",
                formatCurrency(currentBudget.getSpentAmount()),
                formatCurrency(currentBudget.getLimitAmount()));
        tvSpentAmount.setText(spentText);

        // Set percentage
        tvProgressPercentage.setText(progress + "%");

        // Change progress color if over budget
        if (currentBudget.isOverBudget()) {
            progressBar.setProgressDrawable(
                    getResources().getDrawable(R.drawable.progress_gradient_red));
        }

        // Set actual spending (per day)
        double dailySpending = currentBudget.getSpentAmount() / 30; // Assuming 30 days
        tvActualSpending.setText(formatCurrency(dailySpending) + " đ /ngày");

        // Set recommended spending
        tvRecommendedSpending.setText(
                formatCurrency(currentBudget.getRecommendedAmount()) + " đ /ngày");

        // Set estimated spending
        tvEstimatedSpending.setText(
                formatCurrency(currentBudget.getEstimatedAmount()) + " đ");

        // Load expense details dynamically
        loadExpenseDetailsViews();
    }

    private void loadExpenseDetailsViews() {
        llExpenseDetailsList.removeAllViews();

        for (ExpenseDetail detail : expenseDetails) {
            if (detail.getTitle().equals("Chi tiết khoản chi")) continue;

            View itemView = getLayoutInflater().inflate(
                    R.layout.item_expense_detail, llExpenseDetailsList, false);

            ImageView ivIcon = itemView.findViewById(R.id.ivExpenseIcon);
            TextView tvTitle = itemView.findViewById(R.id.tvExpenseTitle);
            TextView tvAmount = itemView.findViewById(R.id.tvExpenseAmount);

            ivIcon.setImageResource(detail.getIconRes());
            tvTitle.setText(detail.getTitle());

            if (detail.getAmount() > 0) {
                String amountText = formatCurrency(detail.getAmount()) + " đ" +
                        detail.getFrequency();
                tvAmount.setText(amountText);
            } else {
                tvAmount.setVisibility(View.GONE);
            }

            llExpenseDetailsList.addView(itemView);
        }
    }

    private void toggleExpenseDetails() {
        if (isExpenseDetailsExpanded) {
            // Collapse
            llExpenseDetailsList.setVisibility(View.GONE);
            isExpenseDetailsExpanded = false;
        } else {
            // Expand
            llExpenseDetailsList.setVisibility(View.VISIBLE);
            isExpenseDetailsExpanded = true;
        }
    }

    private void animateProgress(final ProgressBar progressBar,
                                 int from, int to) {
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                progressBar.setProgress(value);
            }
        });
        animator.start();
    }

    private String formatCurrency(double amount) {
        return decimalFormat.format(amount);
    }
}