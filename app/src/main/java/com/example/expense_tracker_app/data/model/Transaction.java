package com.example.expense_tracker_app.data.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.expense_tracker_app.data.model.converter.Converters;

import java.time.LocalDate;
import java.util.Random;

@Entity(tableName = "transactions")
@TypeConverters(Converters.class)
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public TxType type;
    public Category category;
    public int subcategoryId;
    public String subcategoryName;
    public String subcategoryIcon;
    public long amount;
    public String method;
    public LocalDate date;
    public String note;
    public String location;
    public String imagePath;

    // --- MỚI: Thêm cột này ---
    public boolean excludeFromReport;

    public Transaction(int id,
                       int userId,
                       TxType type,
                       Category category,
                       int subcategoryId,
                       String subcategoryName,
                       String subcategoryIcon,
                       long amount,
                       String method,
                       LocalDate date,
                       String note,
                       String location,
                       String imagePath,
                       boolean excludeFromReport) { // Cập nhật constructor
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.category = category;
        this.subcategoryId = subcategoryId;
        this.subcategoryName = subcategoryName;
        this.subcategoryIcon = subcategoryIcon;
        this.amount = amount;
        this.method = method;
        this.date = date;
        this.note = note;
        this.location = location;
        this.imagePath = imagePath;
        this.excludeFromReport = excludeFromReport;
    }

    @Ignore
    public Transaction(int id, TxType type, Category category, long amount, String method, LocalDate date, String note, int userId) {
        this(id, userId, type, category, 0, "", "", amount, method, date, note, "", "", false);
    }

    @Ignore
    public Transaction() {
        this(0, 1, TxType.EXPENSE, new Category("Khác"), 0, "", "", 0L, "Tiền mặt", LocalDate.now(), "", "", "", false);
    }

    public static Transaction fake(String name, long amount, TxType type, String method){
        Category fakeCat = new Category(name);
        return new Transaction(
                new Random().nextInt(1000),
                1,
                type,
                fakeCat,
                0,
                fakeCat.name,
                fakeCat.icon,
                amount,
                method,
                LocalDate.now(),
                "",
                "",
                "",
                false
        );
    }
}