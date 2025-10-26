package com.example.expense_tracker_app.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.expense_tracker_app.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Chọn thời gian: ngày/tuần/tháng/quý/năm. Chỉ so sánh đồng nhất.
 *  Tuần: render đúng 1 năm đang duyệt, 4 tuần/hàng.
 *  Năm hiện tại: tuần hiện tại ở đầu. Quá khứ: tuần cuối năm ở đầu. Tương lai: tuần đầu năm ở đầu. */
public class CalendarSheet extends BottomSheetDialogFragment {

    public interface OnApplySelectionListener { void onApply(List<Period> periods); }
    public enum Mode { DAY, WEEK, MONTH, QUARTER, YEAR }

    private Mode mode = Mode.DAY;
    private final ArrayList<Period> selected = new ArrayList<>();

    public static CalendarSheet newInstance(List<Period> init){
        CalendarSheet s = new CalendarSheet();
        if (init != null && !init.isEmpty()){
            s.mode = init.get(0).type;
            s.selected.addAll(init);
        }
        return s;
    }

    private LinearLayout container;
    private TextView tvTitle, tvModeDay, tvModeWeek, tvModeMonth, tvModeQuarter, tvModeYear;

    private YearMonth browsingMonth;
    private int browsingYearWeeks = 0;
    private int browsingYearMonths = 0;    // state cho tab Tháng
    private int browsingYearQuarters = 0;  // state cho tab Quý

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup parent, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.sheet_calendar, parent, false);
        container     = v.findViewById(R.id.container);
        tvTitle       = v.findViewById(R.id.tvTitle);
        tvModeDay     = v.findViewById(R.id.tabDay);
        tvModeWeek    = v.findViewById(R.id.tabWeek);
        tvModeMonth   = v.findViewById(R.id.tabMonth);
        tvModeQuarter = v.findViewById(R.id.tabQuarter);
        tvModeYear    = v.findViewById(R.id.tabYear);

        View btnApply = v.findViewById(R.id.btnApply);
        View btnClear = v.findViewById(R.id.btnClear);
        ImageView btnClose = v.findViewById(R.id.btnClose);

        View.OnClickListener tabs = x -> {
            if (isLocked() && !isCurrentTab(x)) {
                Toast.makeText(requireContext(), "Bỏ chọn mục đang chọn để đổi chế độ.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (x == tvModeDay) mode = Mode.DAY;
            else if (x == tvModeWeek) mode = Mode.WEEK;
            else if (x == tvModeMonth) mode = Mode.MONTH;
            else if (x == tvModeQuarter) mode = Mode.QUARTER;
            else mode = Mode.YEAR;
            render();
        };
        tvModeDay.setOnClickListener(tabs);
        tvModeWeek.setOnClickListener(tabs);
        tvModeMonth.setOnClickListener(tabs);
        tvModeQuarter.setOnClickListener(tabs);
        tvModeYear.setOnClickListener(tabs);

        btnClear.setOnClickListener(v13 -> { selected.clear(); updateTabsUI(); render(); });
        btnApply.setOnClickListener(v1 -> {
            selected.removeIf(p -> p.type != mode);
            if (getActivity() instanceof OnApplySelectionListener) {
                ((OnApplySelectionListener) getActivity()).onApply(new ArrayList<>(selected));
            }
            dismiss();
        });
        btnClose.setOnClickListener(v12 -> dismiss());

        render();
        return v;
    }

    private void render() {
        container.removeAllViews();
        LocalDate now = LocalDate.now();
        if (browsingMonth == null) browsingMonth = YearMonth.of(now.getYear(), now.getMonthValue());
        if (browsingYearWeeks == 0) browsingYearWeeks = now.getYear();
        if (browsingYearMonths == 0) browsingYearMonths = now.getYear();
        if (browsingYearQuarters == 0) browsingYearQuarters = now.getYear();

        tvTitle.setText(titleFor(mode));

        switch (mode) {
            case DAY:     renderDays(browsingMonth); break;
            case WEEK:    renderWeeks(); break;
            case MONTH:   renderMonths(browsingYearMonths); break;
            case QUARTER: renderQuarters(browsingYearQuarters); break;
            case YEAR:    renderYears(/*ignored*/0); break;
        }
        updateTabsUI();
    }

    private boolean isLocked(){ return !selected.isEmpty(); }
    private boolean isCurrentTab(View x){
        return (mode == Mode.DAY && x == tvModeDay)
                || (mode == Mode.WEEK && x == tvModeWeek)
                || (mode == Mode.MONTH && x == tvModeMonth)
                || (mode == Mode.QUARTER && x == tvModeQuarter)
                || (mode == Mode.YEAR && x == tvModeYear);
    }

    private void updateTabsUI(){
        int sel = requireContext().getColor(R.color.primary_1);
        int nor = requireContext().getColor(R.color.text_secondary);
        tvModeDay.setTextColor(mode == Mode.DAY ? sel : nor);
        tvModeWeek.setTextColor(mode == Mode.WEEK ? sel : nor);
        tvModeMonth.setTextColor(mode == Mode.MONTH ? sel : nor);
        tvModeQuarter.setTextColor(mode == Mode.QUARTER ? sel : nor);
        tvModeYear.setTextColor(mode == Mode.YEAR ? sel : nor);

        boolean locked = isLocked();
        setTabState(tvModeDay, mode == Mode.DAY || !locked);
        setTabState(tvModeWeek, mode == Mode.WEEK || !locked);
        setTabState(tvModeMonth, mode == Mode.MONTH || !locked);
        setTabState(tvModeQuarter, mode == Mode.QUARTER || !locked);
        setTabState(tvModeYear, mode == Mode.YEAR || !locked);
    }
    private void setTabState(TextView tv, boolean enabled){
        tv.setEnabled(enabled);
        tv.setAlpha(enabled ? 1f : 0.45f);
    }

    private String titleFor(Mode m){
        switch (m){
            case DAY: return "Chọn ngày lập biểu đồ";
            case WEEK: return "Chọn tuần";
            case MONTH: return "Chọn tháng";
            case QUARTER: return "Chọn quý";
            default: return "Chọn năm";
        }
    }

    /* ===== RENDERERS ===== */

    // DAY
    private void renderDays(YearMonth ym){
        addMonthHeader(ym, () -> { browsingMonth = browsingMonth.minusMonths(1); render(); },
                () -> { browsingMonth = browsingMonth.plusMonths(1); render(); });

        addWeekdayBar();

        LocalDate first = ym.atDay(1);
        int daysInMonth = ym.lengthOfMonth();

        int dowFirst = first.getDayOfWeek().getValue(); // Mon=1..Sun=7
        int leadingBlanks = (dowFirst + 6) % 7;

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        container.addView(row);

        for (int i=0;i<leadingBlanks;i++) addEmptyCell(row);

        for (int d=1; d<=daysInMonth; d++){
            if (row.getChildCount() == 7){
                row = new LinearLayout(requireContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                container.addView(row);
            }
            LocalDate date = first.plusDays(d-1);
            addChoice(row, String.format(Locale.US, "%02d", d), Period.forDay(date), 1f);
        }
        while (row.getChildCount() < 7) addEmptyCell(row);
    }

    // WEEK: 1 năm đang duyệt. 4 tuần/hàng.
    private void renderWeeks(){
        addYearHeader(browsingYearWeeks,
                () -> { browsingYearWeeks -= 1; render(); },
                () -> { browsingYearWeeks += 1; render(); });


        final int year = browsingYearWeeks;
        final int curYear = LocalDate.now().getYear();
        final LocalDate curMon = LocalDate.now().with(DayOfWeek.MONDAY);

        List<LocalDate> mondaysAsc = weeksOfYear(year);
        mondaysAsc.sort(LocalDate::compareTo);

        List<LocalDate> ordered = new ArrayList<>();
        LocalDate currentMarker = null;

        if (year == curYear) {
            List<LocalDate> future = new ArrayList<>();
            List<LocalDate> past   = new ArrayList<>();
            for (LocalDate m : mondaysAsc) {
                if (!m.isBefore(curMon)) future.add(m);
                else past.add(m);
            }
            // tuần cuối -> ... -> tuần hiện tại
            for (int i = future.size() - 1; i >= 0; i--) {
                LocalDate m = future.get(i);
                ordered.add(m);
                if (m.equals(curMon)) currentMarker = m;
            }
            past.sort((a,b) -> b.compareTo(a)); // gần -> xa
            ordered.addAll(past);
        } else if (year < curYear) {
            for (int i = mondaysAsc.size()-1; i >= 0; i--) ordered.add(mondaysAsc.get(i));
        } else {
            ordered.addAll(mondaysAsc);
        }

        LinearLayout row = null, currentRow = null, firstRow = null;
        int col = 0;
        for (LocalDate m : ordered) {
            if (row == null || col == 4) {
                row = new LinearLayout(requireContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                container.addView(row);
                if (firstRow == null) firstRow = row;
                col = 0;
            }
            addWeekCell(row, m);
            if (year == curYear && m.equals(currentMarker)) currentRow = row;
            col++;
        }
        if (row != null && col > 0 && col < 4) while (col++ < 4) addEmptyCell(row);

        final LinearLayout scrollRow = (year == curYear && currentRow != null) ? currentRow : firstRow;
        container.post(() -> {
            View parent = (View) container.getParent();
            if (parent != null && scrollRow != null) parent.scrollTo(0, scrollRow.getTop());
        });
    }

    // MONTH: dùng state năm, không render chồng
    private void renderMonths(int year){
        addYearHeader(
                year,
                () -> { browsingYearMonths = year - 1; render(); },
                () -> { browsingYearMonths = year + 1; render(); }
        );

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        container.addView(row);

        for (int m=1;m<=12;m++){
            if (row.getChildCount()==4){
                row = new LinearLayout(requireContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                container.addView(row);
            }
            addChoice(row, "Tháng " + m, Period.forMonth(year, m), 1f);
        }
    }

    // QUARTER: giống MONTH, dùng state riêng
    private void renderQuarters(int year){
        addYearHeader(
                year,
                () -> { browsingYearQuarters = year - 1; render(); },
                () -> { browsingYearQuarters = year + 1; render(); }
        );

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        container.addView(row);

        for (int q=1;q<=4;q++){
            addChoice(row, "Q" + q, Period.forQuarter(year, q), 1f);
        }
    }

    // YEAR: render 12 năm cố định 2020–2032, 4 cột/hàng
    private void renderYears(int ignored){
        int start = 2020, end = 2031; // inclusive
        LinearLayout row = null;
        int col = 0;
        for (int y = start; y <= end; y++){
            if (row == null || col == 4){
                row = new LinearLayout(requireContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                container.addView(row);
                col = 0;
            }
            addChoice(row, String.valueOf(y), Period.forYear(y), 1f);
            col++;
        }
        if (row != null && col>0 && col<4) while (col++<4) addEmptyCell(row);
    }

    /* ===== HELPERS UI ===== */

    private void addMonthHeader(YearMonth ym, Runnable prev, Runnable next){
        View header = LayoutInflater.from(getContext()).inflate(R.layout.view_year_header, container, false);
        TextView tv = header.findViewById(R.id.tvYear);
        tv.setText(String.format(Locale.US, "Tháng %02d - %d", ym.getMonthValue(), ym.getYear()));
        header.findViewById(R.id.ivPrev).setOnClickListener(v -> prev.run());
        header.findViewById(R.id.ivNext).setOnClickListener(v -> next.run());
        container.addView(header);
    }

    private void addWeekdayBar(){
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        container.addView(row);

        String[] wd = {"T2","T3","T4","T5","T6","T7","CN"};
        for (String s : wd){
            TextView t = new TextView(requireContext());
            t.setText(s);
            t.setGravity(android.view.Gravity.CENTER);
            t.setTextColor(requireContext().getColor(R.color.text_secondary));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(8,8,8,8);
            row.addView(t, lp);
        }
    }

    private void addYearHeader(int year, Runnable prev, Runnable next){
        View header = LayoutInflater.from(getContext()).inflate(R.layout.view_year_header, container, false);
        TextView tv = header.findViewById(R.id.tvYear);
        tv.setText(String.valueOf(year));
        header.findViewById(R.id.ivPrev).setOnClickListener(v -> prev.run());
        header.findViewById(R.id.ivNext).setOnClickListener(v -> next.run());
        container.addView(header);
    }

    private void addChoice(ViewGroup parent, String label, Period p, float weight){
        CheckedTextView c = (CheckedTextView) LayoutInflater.from(getContext())
                .inflate(R.layout.view_choice_chip, parent, false);
        c.setText(label);
        c.setGravity(android.view.Gravity.CENTER);
        c.setChecked(contains(p));

        c.setOnClickListener(v -> {
            if (p.type != mode) return;
            if (contains(p)) selected.removeIf(x -> x.key.equals(p.key));
            else selected.add(p);
            c.setChecked(contains(p));
            updateTabsUI();
        });

        if (weight > 0){
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
            lp.setMargins(8,8,8,8);
            parent.addView(c, lp);
        } else {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(16,8,16,8);
            parent.addView(c, lp);
        }
    }

    private void addWeekCell(ViewGroup row, LocalDate monday){
        LocalDate s = monday;
        LocalDate e = s.plusDays(6);
        String label = String.format(Locale.US,"%02d/%02d - %02d/%02d",
                s.getDayOfMonth(), s.getMonthValue(), e.getDayOfMonth(), e.getMonthValue());
        addChoice(row, label, Period.forWeek(s, e), 1f);
    }

    private void addEmptyCell(ViewGroup row){
        TextView t = new TextView(requireContext());
        t.setText("");
        t.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(8,8,8,8);
        row.addView(t, lp);
    }

    private boolean contains(Period p){
        for (Period x: selected) if (x.key.equals(p.key)) return true;
        return false;
    }

    /* ===== UTILS: tuần của một năm ===== */
    private List<LocalDate> weeksOfYear(int year){
        LocalDate jan1  = LocalDate.of(year, 1, 1);
        LocalDate dec31 = LocalDate.of(year, 12, 31);

        LocalDate firstMonday = jan1.with(DayOfWeek.MONDAY);
        if (firstMonday.isAfter(jan1)) firstMonday = firstMonday.minusWeeks(1);

        List<LocalDate> res = new ArrayList<>();
        for (LocalDate m = firstMonday; !m.isAfter(dec31.plusWeeks(1)); m = m.plusWeeks(1)) {
            LocalDate end = m.plusDays(6);
            if (m.getYear() == year || end.getYear() == year) res.add(m);
            if (m.isAfter(dec31) && end.isAfter(dec31)) break;
        }
        return res;
    }

    /* ===== DATA ===== */
    public static class Period {
        public final String key;
        public final String label;
        public final Mode type;

        private Period(String key, String label, Mode type){
            this.key = key; this.label = label; this.type = type;
        }
        public static Period forDay(LocalDate d){
            return new Period("D:"+d, String.format(Locale.US,"%02d/%02d", d.getDayOfMonth(), d.getMonthValue()), Mode.DAY);
        }
        public static Period forWeek(LocalDate s, LocalDate e){
            return new Period("W:"+s+"_"+e, String.format(Locale.US,"%02d/%02d - %02d/%02d",
                    s.getDayOfMonth(), s.getMonthValue(), e.getDayOfMonth(), e.getMonthValue()), Mode.WEEK);
        }
        public static Period forMonth(int y, int m){
            return new Period("M:"+y+"-"+m, "T"+m+"/"+(y%100), Mode.MONTH);
        }
        public static Period forQuarter(int y, int q){
            return new Period("Q:"+y+"-"+q, "Q"+q+"/"+(y%100), Mode.QUARTER);
        }
        public static Period forYear(int y){
            return new Period("Y:"+y, String.valueOf(y), Mode.YEAR);
        }
        public String label(){ return label; }
    }
}
