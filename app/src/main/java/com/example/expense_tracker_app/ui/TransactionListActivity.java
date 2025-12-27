package com.example.expense_tracker_app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Category;
import com.example.expense_tracker_app.data.model.Transaction;
import com.example.expense_tracker_app.data.model.TxType;
import com.example.expense_tracker_app.data.repository.TransactionRepository;
import com.example.expense_tracker_app.ui.Month.MonthAdapter;
import com.example.expense_tracker_app.ui.Month.MonthItem;
import com.example.expense_tracker_app.ui.adapter.TransactionAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransactionListActivity extends AppCompatActivity {

    private static final String TAG = "TransactionListActivity";
    
    private RecyclerView rvList, rvMonths;
    private TextView tvEmpty;
    private ImageButton btnExport;
    private TransactionAdapter adapter;
    private TransactionRepository repository;
    private MonthAdapter monthAdapter;
    
    private TxType filterType;
    private LocalDate selectedMonth;
    
    private final List<MonthItem> monthItems = new ArrayList<>();
    private List<Transaction> currentTransactions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_transaction_list);

        // Nhận Intent data
        String typeStr = getIntent().getStringExtra("tx_type");
        filterType = (typeStr != null) ? TxType.valueOf(typeStr) : TxType.EXPENSE;

        int month = getIntent().getIntExtra("month", LocalDate.now().getMonthValue());
        int year = getIntent().getIntExtra("year", LocalDate.now().getYear());
        selectedMonth = LocalDate.of(year, month, 1);

        // Init views
        repository = new TransactionRepository(getApplication());
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnExport = findViewById(R.id.btnExport);
        TextView tvTitle = findViewById(R.id.tvTitle);
        rvList = findViewById(R.id.rvList);
        rvMonths = findViewById(R.id.rvMonths);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Setup title
        String title = (filterType == TxType.INCOME) ? "Chi tiết Thu nhập" : "Chi tiết Chi tiêu";
        tvTitle.setText(title);

        // Setup RecyclerView giao dịch
        adapter = new TransactionAdapter(this);
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(adapter);

        // Setup RecyclerView tháng
        setupMonthStrip();

        // Event listeners
        btnBack.setOnClickListener(v -> finish());
        btnExport.setOnClickListener(v -> showExportDialog());
    }

    private void setupMonthStrip() {
        monthItems.clear();
        
        LocalDate now = LocalDate.now().minusMonths(24);
        for (int i = 0; i <= 48; i++) {
            LocalDate d = now.plusMonths(i);
            monthItems.add(new MonthItem(d.getYear(), d.getMonthValue()));
        }

        LinearLayoutManager lm = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        rvMonths.setLayoutManager(lm);
        
        monthAdapter = new MonthAdapter(monthItems);
        rvMonths.setAdapter(monthAdapter);

        PagerSnapHelper snap = new PagerSnapHelper();
        snap.attachToRecyclerView(rvMonths);

        int selectedIndex = findMonthIndex(monthItems, selectedMonth.getYear(), selectedMonth.getMonthValue());

        rvMonths.post(() -> {
            monthAdapter.selected = selectedIndex;
            rvMonths.scrollToPosition(selectedIndex);
            monthAdapter.notifyDataSetChanged();
            
            loadTransactions();
        });

        rvMonths.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView r, int state) {
                if (state != RecyclerView.SCROLL_STATE_IDLE) return;
                
                View v = snap.findSnapView(lm);
                if (v == null) return;
                
                int idx = lm.getPosition(v);
                if (idx == RecyclerView.NO_POSITION || idx == monthAdapter.selected) return;
                
                monthAdapter.selected = idx;
                monthAdapter.notifyDataSetChanged();
                
                MonthItem cur = monthItems.get(idx);
                selectedMonth = LocalDate.of(cur.year, cur.month, 1);
                
                loadTransactions();
            }
        });
    }

    private int findMonthIndex(List<MonthItem> list, int year, int month) {
        for (int i = 0; i < list.size(); i++) {
            MonthItem it = list.get(i);
            if (it.year == year && it.month == month) return i;
        }
        return list.size() / 2;
    }

    private void loadTransactions() {
        repository.getTransactionsByMonth(1, selectedMonth).observe(this, allTransactions -> {
            if (allTransactions == null || allTransactions.isEmpty()) {
                showEmptyState();
                return;
            }
            
            List<Transaction> filtered = new ArrayList<>();
            for (Transaction tx : allTransactions) {
                if (tx.type == filterType) {
                    filtered.add(tx);
                }
            }
            
            if (filtered.isEmpty()) {
                showEmptyState();
            } else {
                hideEmptyState();
                currentTransactions = filtered;
                adapter.setData(filtered);
                Log.d(TAG, "Loaded " + filtered.size() + " transactions for " + 
                      selectedMonth.getMonthValue() + "/" + selectedMonth.getYear());
            }
        });
    }

    private void showEmptyState() {
        currentTransactions.clear();
        rvList.setVisibility(View.GONE);
        
        if (tvEmpty != null) {
            String emptyText = (filterType == TxType.INCOME) 
                ? "💸 Ví trống rỗng...\n\nChưa có đồng thu nhập nào cả!\n\n(Đi làm thêm đi bạn ơi 😅)"
                : "🎉 Tháng này bạn tiết kiệm quá!\n\nKhông có khoản chi nào?\n\nHay bạn quên ghi rồi? 🤔";
            
            tvEmpty.setText(emptyText);
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.bringToFront();
        }
    }
    
    private void hideEmptyState() {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(View.GONE);
        }
        rvList.setVisibility(View.VISIBLE);
    }

    // ==================== EXPORT DIALOG ====================
    
    private void showExportDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_export_options, null);
        dialog.setContentView(view);

        // Ánh xạ views
        View btnCloseDialog = view.findViewById(R.id.btnCloseDialog);
        LinearLayout btnExportData = view.findViewById(R.id.btnExportData);
        LinearLayout btnShareData = view.findViewById(R.id.btnShareData);

        // Đóng dialog
        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        // Xuất dữ liệu
        btnExportData.setOnClickListener(v -> {
            dialog.dismiss();
            exportToExcel();
        });

        // Chia sẻ dữ liệu
        btnShareData.setOnClickListener(v -> {
            dialog.dismiss();
            shareData();
        });

        dialog.show();
    }

    private void exportToExcel() {
        if (currentTransactions.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu để xuất", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Tạo Workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Giao dịch");

            // Tạo header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Tạo header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Ngày", "Danh mục", "Phương thức", "Số tiền", "Ghi chú", "Địa điểm"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Định dạng ngày
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // Thêm dữ liệu
            int rowNum = 1;
            for (Transaction tx : currentTransactions) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(tx.date.format(dateFormatter));
                row.createCell(1).setCellValue(tx.subcategoryName != null && !tx.subcategoryName.isEmpty() 
                    ? tx.subcategoryName : tx.category.name);
                row.createCell(2).setCellValue(tx.method);
                row.createCell(3).setCellValue(tx.amount);
                row.createCell(4).setCellValue(tx.note != null ? tx.note : "");
                row.createCell(5).setCellValue(tx.location != null ? tx.location : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Lưu file
            String fileName = String.format("GiaoDich_%s_T%d_%d.xlsx", 
                filterType == TxType.INCOME ? "ThuNhap" : "ChiTieu",
                selectedMonth.getMonthValue(), 
                selectedMonth.getYear());
            
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);

            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            outputStream.close();
            workbook.close();

            Toast.makeText(this, "Đã xuất file: " + fileName, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi xuất file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void shareData() {
        if (currentTransactions.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu để chia sẻ", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Tạo file Excel tương tự như export
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Giao dịch");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Ngày", "Danh mục", "Phương thức", "Số tiền", "Ghi chú", "Địa điểm"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            int rowNum = 1;
            for (Transaction tx : currentTransactions) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(tx.date.format(dateFormatter));
                row.createCell(1).setCellValue(tx.subcategoryName != null && !tx.subcategoryName.isEmpty() 
                    ? tx.subcategoryName : tx.category.name);
                row.createCell(2).setCellValue(tx.method);
                row.createCell(3).setCellValue(tx.amount);
                row.createCell(4).setCellValue(tx.note != null ? tx.note : "");
                row.createCell(5).setCellValue(tx.location != null ? tx.location : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Lưu file vào cache
            String fileName = String.format("GiaoDich_%s_T%d_%d.xlsx", 
                filterType == TxType.INCOME ? "ThuNhap" : "ChiTieu",
                selectedMonth.getMonthValue(), 
                selectedMonth.getYear());
            
            File cacheDir = getCacheDir();
            File file = new File(cacheDir, fileName);

            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            outputStream.close();
            workbook.close();

            // Chia sẻ file
            Uri fileUri = FileProvider.getUriForFile(this, 
                getApplicationContext().getPackageName() + ".fileprovider", file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Dữ liệu giao dịch - " + 
                (filterType == TxType.INCOME ? "Thu nhập" : "Chi tiêu"));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi chia sẻ: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}