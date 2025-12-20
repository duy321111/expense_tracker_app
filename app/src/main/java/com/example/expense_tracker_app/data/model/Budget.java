package com.example.expense_tracker_app.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.TypeConverters;
import com.example.expense_tracker_app.data.model.converter.StringListConverter;
import java.util.List;

@Entity(tableName = "budgets")
@TypeConverters(StringListConverter.class)
public class Budget {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "categories")
    private List<String> categories;
    @ColumnInfo(name = "amount")
    private double amount;
    @ColumnInfo(name = "spent_amount")
    private double spentAmount;
    @ColumnInfo(name = "period")
    private String period;
    @ColumnInfo(name = "created_at")
    private long createdAt;
    @ColumnInfo(name = "month")
    private int month;
    @ColumnInfo(name = "year")
    private int year;

    public Budget(String name, List<String> categories, double amount, double spentAmount,
                  String period, long createdAt, int month, int year) {
        this.name = name;
        this.categories = categories;
        this.amount = amount;
        this.spentAmount = spentAmount;
        this.period = period;
        this.createdAt = createdAt;
        this.month = month;
        this.year = year;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public double getSpentAmount() { return spentAmount; }
    public void setSpentAmount(double spentAmount) { this.spentAmount = spentAmount; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
}
