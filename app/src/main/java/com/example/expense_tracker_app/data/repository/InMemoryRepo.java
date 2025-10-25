package com.example.expense_tracker_app.data.repository;

import com.example.expense_tracker_app.data.model.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryRepo implements Repository {
    // Categories được cập nhật theo cấu trúc mới
    private final List<Category> cats = Arrays.asList(
            new Category(1,"Ăn uống", TxType.EXPENSE,"ic_cat_food"),
            new Category(2,"Cà phê", TxType.EXPENSE,"ic_cat_coffee"),
            new Category(3,"Trả nợ", TxType.EXPENSE,"ic_cat_debt"),
            new Category(4,"Thuê nhà", TxType.EXPENSE,"ic_cat_home"),
            new Category(5,"Tiền lương", TxType.INCOME,"ic_cat_salary"),
            new Category(6,"Bán hàng", TxType.INCOME,"ic_cat_sale"),
            new Category(7,"Được trả nợ", TxType.INCOME,"ic_cat_debt_return"),
            new Category(8,"Đi vay", TxType.BORROW,"ic_cat_borrow"),
            new Category(9,"Cho vay", TxType.LEND,"ic_cat_lend"),
            new Category(10,"Điều chỉnh", TxType.ADJUST,"ic_cat_adjust")
    );
    private final List<Transaction> store = new ArrayList<>();

    public InMemoryRepo(){
        LocalDate now = LocalDate.now();
        Random r = new Random(7);

        // Dữ liệu mockups chính
        store.add(new Transaction(101, TxType.INCOME, cats.get(5), 400_000, "Tiền mặt", LocalDate.of(now.getYear(), now.getMonth(), 8), "Bán hàng"));
        store.add(new Transaction(102, TxType.INCOME, cats.get(4), 3_000_000, "Chuyển khoản", LocalDate.of(now.getYear(), now.getMonth(), 10), "Tiền lương"));
        store.add(new Transaction(103, TxType.INCOME, cats.get(6), 2_500_000, "Chuyển khoản", LocalDate.of(now.getYear(), now.getMonth(), 10), "Được trả nợ"));

        store.add(new Transaction(104, TxType.EXPENSE, cats.get(0), 99_000, "Tiền mặt", LocalDate.of(now.getYear(), now.getMonth(), 4), "Ăn uống"));
        store.add(new Transaction(105, TxType.EXPENSE, cats.get(1), 150_000, "Chuyển khoản", LocalDate.of(now.getYear(), now.getMonth(), 4), "Cà phê"));
        store.add(new Transaction(106, TxType.EXPENSE, cats.get(2), 420_000, "Tiền mặt", LocalDate.of(now.getYear(), now.getMonth(), 4), "Trả nợ"));
        store.add(new Transaction(107, TxType.EXPENSE, cats.get(3), 1_500_000, "Chuyển khoản", LocalDate.of(now.getYear(), now.getMonth(), 5), "Thuê nhà"));

        // Thêm dữ liệu giả lập cho biểu đồ
        for(int i=0;i<40;i++){
            int day = i%28+1;
            boolean inc = i%3==0;
            Category c = inc? cats.get(5):cats.get(0);
            if (day==4 || day==5 || day==8 || day==10) continue;

            store.add(new Transaction(i+200, inc?TxType.INCOME:TxType.EXPENSE, c,
                    50_000 + r.nextInt(2_900_000), i%2==0? "Tiền mặt":"Chuyển khoản",
                    LocalDate.of(now.getYear(), now.getMonth(), day), inc?"Thu nhập khác":"Chi tiêu khác"));
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
        int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();

        for(int d=1; d<=daysInMonth; d++){
            List<Transaction> list = g.getOrDefault(d, Collections.emptyList());
            long in = list.stream().filter(t->t.type==TxType.INCOME).mapToLong(t->t.amount).sum();
            long ex = list.stream().filter(t->t.type==TxType.EXPENSE).mapToLong(t->t.amount).sum();
            res.add(new MonthlyStat(d,in,ex));
        }
        return res;
    }
}