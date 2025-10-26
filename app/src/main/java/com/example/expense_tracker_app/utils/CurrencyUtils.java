package com.example.expense_tracker_app.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {

    private static final DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private static final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public static String formatCurrency(double amount) {
        return decimalFormat.format(amount);
    }

    public static String formatCurrencyWithSymbol(double amount) {
        return decimalFormat.format(amount) + " đ";
    }

    public static String formatCurrencyFull(double amount) {
        return currencyFormat.format(amount);
    }

    public static String formatPercentage(int percentage) {
        return percentage + "%";
    }

    public static String formatDailyAmount(double amount) {
        return formatCurrency(amount) + " đ /ngày";
    }

    public static String formatMonthlyAmount(double amount) {
        return formatCurrency(amount) + " đ /tháng";
    }
}