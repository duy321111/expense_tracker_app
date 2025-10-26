package com.example.expense_tracker_app.data.repository;

import com.example.expense_tracker_app.data.model.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryRepo implements Repository {

    private static InMemoryRepo INSTANCE;

    private final Map<TxType, List<Category>> categories = new EnumMap<>(TxType.class);
    private final List<Transaction> transactions = new ArrayList<>();

    public InMemoryRepo() {
        for (TxType t : TxType.values()) categories.put(t, new ArrayList<>());
        seedCategories();
    }

    public static synchronized InMemoryRepo get() {
        if (INSTANCE == null) INSTANCE = new InMemoryRepo();
        return INSTANCE;
    }

    private void seedCategories() {
        // EXPENSE
        categories.get(TxType.EXPENSE).add(new Category("Ăn uống"));
        categories.get(TxType.EXPENSE).add(new Category("Cà phê"));
        categories.get(TxType.EXPENSE).add(new Category("Đi chợ/Siêu thị"));
        categories.get(TxType.EXPENSE).add(new Category("Điện"));
        categories.get(TxType.EXPENSE).add(new Category("Nước"));
        categories.get(TxType.EXPENSE).add(new Category("Internet"));
        categories.get(TxType.EXPENSE).add(new Category("Di chuyển"));
        categories.get(TxType.EXPENSE).add(new Category("Thuê nhà"));
        categories.get(TxType.EXPENSE).add(new Category("Thể thao"));
        categories.get(TxType.EXPENSE).add(new Category("Du lịch"));
        categories.get(TxType.EXPENSE).add(new Category("Giải trí"));

        // INCOME
        categories.get(TxType.INCOME).add(new Category("Lương"));
        categories.get(TxType.INCOME).add(new Category("Thưởng"));
        categories.get(TxType.INCOME).add(new Category("Khác"));
    }

    @Override
    public List<Category> categoriesBy(TxType type) {
        List<Category> list = categories.get(type);
        return list == null ? Collections.emptyList() : new ArrayList<>(list);
    }

    @Override
    public synchronized void addCategory(Category c) {
        categories.get(TxType.EXPENSE).add(c);
    }

    @Override
    public synchronized void addCategory(Category c, TxType type) {
        categories.get(type).add(c);
    }

    @Override
    public synchronized void addTransaction(Transaction t) {
        transactions.add(t);
    }

    @Override
    public List<Transaction> transactionsByMonth(int year, int month) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.date.getYear() == year && t.date.getMonthValue() == month)
                result.add(t);
        }
        return result;
    }

    @Override
    public List<MonthlyStat> dailyStats(int year, int month) {
        Map<Integer, List<Transaction>> grouped = transactionsByMonth(year, month)
                .stream()
                .collect(Collectors.groupingBy(t -> t.date.getDayOfMonth()));

        List<MonthlyStat> result = new ArrayList<>();
        int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();

        for (int d = 1; d <= daysInMonth; d++) {
            List<Transaction> list = grouped.getOrDefault(d, Collections.emptyList());
            long income = list.stream().filter(t -> t.type == TxType.INCOME).mapToLong(t -> t.amount).sum();
            long expense = list.stream().filter(t -> t.type == TxType.EXPENSE).mapToLong(t -> t.amount).sum();
            result.add(new MonthlyStat(d, income, expense));
        }
        return result;
    }
}
