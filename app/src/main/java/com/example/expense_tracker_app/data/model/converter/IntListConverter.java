package com.example.expense_tracker_app.data.model.converter;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class IntListConverter {
    @TypeConverter
    public static List<Integer> fromString(String value) {
        if (value == null || value.isEmpty()) return new ArrayList<>();
        Type listType = new TypeToken<List<Integer>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }
    @TypeConverter
    public static String fromList(List<Integer> list) {
        if (list == null) return "";
        return new Gson().toJson(list);
    }
}
