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
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;            
import java.util.List;
import java.util.Locale;          

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
    private LocalDate fromMonth, toMonth;
    
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

        // Nhận range tháng nếu có
        int fromMonthValue = getIntent().getIntExtra("from_month", -1);
        int fromYearValue = getIntent().getIntExtra("from_year", -1);
        int toMonthValue = getIntent().getIntExtra("to_month", -1);
        int toYearValue = getIntent().getIntExtra("to_year", -1);
        if (fromMonthValue > 0 && fromYearValue > 0 && toMonthValue > 0 && toYearValue > 0) {
            fromMonth = LocalDate.of(fromYearValue, fromMonthValue, 1);
            toMonth = LocalDate.of(toYearValue, toMonthValue, 1);
        } else {
            fromMonth = null;
            toMonth = null;
        }

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
        if (fromMonth != null && toMonth != null && !fromMonth.isAfter(toMonth)) {
            LocalDate iter = fromMonth;
            while (!iter.isAfter(toMonth)) {
                monthItems.add(new MonthItem(iter.getYear(), iter.getMonthValue()));
                iter = iter.plusMonths(1);
            }
        } else {
            LocalDate now = LocalDate.now().minusMonths(24);
            for (int i = 0; i <= 48; i++) {
                LocalDate d = now.plusMonths(i);
                monthItems.add(new MonthItem(d.getYear(), d.getMonthValue()));
            }
        }

        LinearLayoutManager lm = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        rvMonths.setLayoutManager(lm);
        
        monthAdapter = new MonthAdapter(monthItems);
        rvMonths.setAdapter(monthAdapter);

        // Callback khi chọn tháng (tap hoặc scroll)
        monthAdapter.setOnMonthSelectedListener((pos, item) -> {
            selectedMonth = LocalDate.of(item.year, item.month, 1);
            loadTransactions();
        });

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

        // Đóng dialog
        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        // Xuất dữ liệu
        btnExportData.setOnClickListener(v -> {
            dialog.dismiss();
            // Lấy danh sách transactions hiện tại từ adapter
            List<Transaction> currentTransactions = adapter.getData();
            if (currentTransactions == null || currentTransactions.isEmpty()) {
                Toast.makeText(this, "Không có dữ liệu để xuất", Toast.LENGTH_SHORT).show();
                return;
            }
            // Tạo tên file với timestamp
            String typePrefix = (filterType == TxType.INCOME) ? "ThuNhap" : "ChiTieu";
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String filename = typePrefix + "_" + timestamp + ".xlsx";
            // Export
            exportToExcel(currentTransactions, filename);
        });

        dialog.show();
    }

    private void exportToExcel(List<Transaction> transactions, String filename) {
        try {
            // Tạo workbook
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Transactions");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Ngày", "Loại", "Danh mục", "Số tiền", "Phương thức", "Ghi chú", "Địa điểm"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));

            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0"));

            int rowNum = 1;
            for (Transaction tx : transactions) {
                Row row = sheet.createRow(rowNum++);
                
                // Date
                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(tx.date.toString());
                dateCell.setCellStyle(dateStyle);
                
                // Type
                row.createCell(1).setCellValue(tx.type == TxType.INCOME ? "Thu nhập" : "Chi tiêu");
                
                // Category
                row.createCell(2).setCellValue(tx.category != null ? tx.category.name : "");
                
                // Amount
                Cell amountCell = row.createCell(3);
                amountCell.setCellValue(tx.amount);
                amountCell.setCellStyle(numberStyle);
                
                // Method
                row.createCell(4).setCellValue(tx.method != null ? tx.method : "");
                
                // Note
                row.createCell(5).setCellValue(tx.note != null ? tx.note : "");
                
                // Location
                row.createCell(6).setCellValue(tx.location != null ? tx.location : "");
            }

            // ❌ XÓA DÒNG NÀY (gây lỗi trên Android):
            // for (int i = 0; i < headers.length; i++) {
            //     sheet.autoSizeColumn(i);
            // }

            // ✅ THAY BẰNG: Set width thủ công
            sheet.setColumnWidth(0, 12 * 256);  // Ngày: 12 chars
            sheet.setColumnWidth(1, 12 * 256);  // Loại: 12 chars
            sheet.setColumnWidth(2, 20 * 256);  // Danh mục: 20 chars
            sheet.setColumnWidth(3, 15 * 256);  // Số tiền: 15 chars
            sheet.setColumnWidth(4, 15 * 256);  // Phương thức: 15 chars
            sheet.setColumnWidth(5, 30 * 256);  // Ghi chú: 30 chars
            sheet.setColumnWidth(6, 20 * 256);  // Địa điểm: 20 chars

            // Save file
            File exportDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "ExpenseTracker");
            
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File file = new File(exportDir, filename);
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            Log.d(TAG, "✅ Excel exported: " + file.getAbsolutePath());
            
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã lưu: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Export error: " + e.getMessage(), e);
            runOnUiThread(() -> {
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }

    // Đã xoá chức năng chia sẻ dữ liệu
}