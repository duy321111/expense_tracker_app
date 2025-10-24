package com.example.expense_tracker_app.data.model;

public class Transaction {
    public String title;
    public long amount;
    public TxType type;
    public String method;
    public Category category;

    public static Transaction fake(String name, long amount, TxType type, String method){
        Transaction t = new Transaction();
        t.title = name; t.amount = amount; t.type = type; t.method = method;
        t.category = new Category(name);
        return t;
    }
}
