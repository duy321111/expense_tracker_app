package com.example.expense_tracker_app.data.model.converter;

import androidx.room.TypeConverter;
import com.example.expense_tracker_app.data.model.Category;
import com.example.expense_tracker_app.data.model.TxType;
import com.google.gson.Gson;
import java.time.LocalDate;

public class Converters {
    // Chuyển đổi LocalDate <-> Long (để lưu ngày tháng)
    @TypeConverter
    public static LocalDate fromTimestamp(Long value) {
        return value == null ? null : LocalDate.ofEpochDay(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(LocalDate date) {
        return date == null ? null : date.toEpochDay();
    }

    // Chuyển đổi TxType (Enum) <-> String
    @TypeConverter
    public static TxType fromTypeString(String value) {
        return value == null ? TxType.EXPENSE : TxType.valueOf(value);
    }

    @TypeConverter
    public static String typeToString(TxType type) {
        return type == null ? TxType.EXPENSE.name() : type.name();
    }

    // Chuyển đổi Category <-> String (JSON)
    // Lưu ý: Cần thêm thư viện Gson vào build.gradle nếu chưa có, hoặc dùng cách đơn giản hơn bên dưới
    @TypeConverter
    public static Category fromCategoryString(String value) {
        return new Gson().fromJson(value, Category.class);
    }

    @TypeConverter
    public static String categoryToString(Category category) {
        return new Gson().toJson(category);
    }
}