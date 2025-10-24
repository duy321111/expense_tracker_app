package com.example.expense_tracker_app.data.repository;

import com.example.expense_tracker_app.data.model.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryRepo implements Repository {
    private final List<Category> cats = Arrays.asList(
            new Category(1,"Ăn uống", TxType.EXPENSE,"•"),
            new Category(2,"Cà phê", TxType.EXPENSE,"•"),
            new Category(3,"Tiền lương", TxType.INCOME,"•"),
            new Category(4,"Bán hàng", TxType.INCOME,"•"),
            new Category(5,"Đi vay", TxType.BORROW,"•"),
            new Category(6,"Cho vay", TxType.LEND,"•"),
            new Category(7,"Điều chỉnh", TxType.ADJUST,"•")
    );
    private final List<Transaction> store = new ArrayList<>();

    public InMemoryRepo(){
        LocalDate now = LocalDate.now();
        Random r = new Random(7);
        for(int i=0;i<40;i++){
            int day = i%28+1;
            boolean inc = i%3==0;
            Category c = inc? cats.get(3):cats.get(0);
            store.add(new Transaction(i, inc?TxType.INCOME:TxType.EXPENSE, c,
                    50_000 + r.nextInt(2_900_000), i%2==0? "Tiền mặt":"Chuyển khoản",
                    LocalDate.of(now.getYear(), now.getMonth(), day), ""));
        }
    }

    @Override public List<Category> categoriesBy(TxType type){
        List<Category> out = new ArrayList<>();
        for (Category c: cats) if (c.type==type) out.add(c);
        return out;
    }

    @Override public void addTransaction(Transaction tx){ store.add(tx); }

    @Override public List<Transaction> transactionsByMonth(int year, int month){
        return store.stream().filter(t-> t.date.getYear()==year && t.date.getMonthValue()==month)
                .sorted((a,b)-> b.date.compareTo(a.date))
                .collect(Collectors.toList());
    }

    @Override public List<MonthlyStat> dailyStats(int year, int month){
        Map<Integer, List<Transaction>> g = transactionsByMonth(year,month).stream()
                .collect(Collectors.groupingBy(t-> t.date.getDayOfMonth()));
        List<MonthlyStat> res = new ArrayList<>();
        for(int d=1; d<=30; d++){
            List<Transaction> list = g.getOrDefault(d, Collections.emptyList());
            long in = list.stream().filter(t->t.type==TxType.INCOME).mapToLong(t->t.amount).sum();
            long ex = list.stream().filter(t->t.type==TxType.EXPENSE).mapToLong(t->t.amount).sum();
            res.add(new MonthlyStat(d,in,ex));
        }
        return res;
    }
}
