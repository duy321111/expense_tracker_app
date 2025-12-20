package com.example.expense_tracker_app.data.model;

import java.time.LocalDate;
import java.util.Random;

public class Transaction {
    public final int id;
    public final TxType type;
    public final Category category;
    public final long amount;
    public final String method;
    public final LocalDate date;
    public final String note;

    public Transaction(int id, TxType type, Category category, long amount, String method, LocalDate date, String note) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.method = method;
        this.date = date;
        this.note = note;
    }

    public Transaction() {
        this(0, TxType.EXPENSE, new Category("Khác"), 0L, "Tiền mặt", LocalDate.now(), "");
    }

    public static Transaction fake(String name, long amount, TxType type, String method){
        Category fakeCat = new Category(name);

        return new Transaction(
                new Random().nextInt(1000),
                type,
                fakeCat,
                amount,
                method,
                LocalDate.now(),
                ""
        );
    }
}
