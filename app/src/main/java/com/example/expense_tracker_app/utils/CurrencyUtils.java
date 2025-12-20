package com.example.expense_tracker_app.utils;

public class CurrencyUtils {
    public static String vnd(long amount){ return String.format("%,d đ", amount); }
}
