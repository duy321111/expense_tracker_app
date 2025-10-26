package com.example.expense_tracker_app.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.adapter.TypePickerAdapter;
import com.example.expense_tracker_app.data.model.Category;
import com.example.expense_tracker_app.data.model.TxType;
import com.example.expense_tracker_app.databinding.FragmentAddBinding;
import com.example.expense_tracker_app.viewmodel.AddTxViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AddTransactionFragment extends Fragment {

    private FragmentAddBinding b;
    private AddTxViewModel vm;
    private boolean showDetails = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        b = FragmentAddBinding.inflate(i, c, false);
        vm = new ViewModelProvider(this).get(AddTxViewModel.class);

        // back
        b.toolbar.setNavigationOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        // menu "Clear"
        b.toolbar.inflateMenu(R.menu.menu_add_tx);
        b.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_clear) {
                resetForm();
                Toast.makeText(requireContext(), "Đã xoá thông tin", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        // mở chọn loại
        View.OnClickListener openType = v -> openTypeSheet();
        b.rowType.setOnClickListener(openType);
        b.ivType.setOnClickListener(openType);

        // mở chọn danh mục
        View.OnClickListener openCat = v -> openCategorySheet();
        b.rowCategory.setOnClickListener(openCat);
        b.ivCategory.setOnClickListener(openCat);

        // chọn phương thức
        b.optCash.setOnClickListener(v -> selectMethod("Tiền mặt"));
        b.optTransfer.setOnClickListener(v -> selectMethod("Chuyển khoản"));
        if (vm.method.getValue() != null) selectMethod(vm.method.getValue());

        // chọn ngày
        b.rowDate.setOnClickListener(v -> showDatePicker());

        // Ẩn chi tiết mặc định
        setDetailsVisible(false);

// Gán nội dung hint cho phần báo cáo
        b.tvReportHint.setText("Không ghi giao dịch này vào báo cáo, dù có ở Tổng quan.");

// Nút toggle chi tiết
        b.btnToggleDetail.setOnClickListener(v -> {
            showDetails = !showDetails;
            setDetailsVisible(showDetails);
            b.btnToggleDetail.setText(showDetails ? "Ẩn chi tiết ⌃" : "Hiển thị chi tiết ⌄");
            b.btnToggleDetail.setBackgroundResource(showDetails
                    ? R.drawable.bg_card_primary_5
                    : R.drawable.bg_card_neutral_50);
        });


        // observe
        vm.date.observe(getViewLifecycleOwner(), d ->
                b.tvDate.setText(String.format(java.util.Locale.getDefault(),
                        "%02d/%02d/%d", d.getDayOfMonth(), d.getMonthValue(), d.getYear())));

        vm.type.observe(getViewLifecycleOwner(), t ->
                b.tvType.setText(t == TxType.INCOME ? "Thu nhập" : "Chi tiêu"));

        vm.category.observe(getViewLifecycleOwner(), cata -> {
            if (cata != null) {
                b.tvCategory.setText(cata.name);
                b.tvCategory.setTextColor(requireContext().getColor(R.color.primary_1));
            } else {
                b.tvCategory.setText("Chọn danh mục");
                b.tvCategory.setTextColor(requireContext().getColor(R.color.neutral_900));
            }
            updateSaveEnabled();
        });

        b.etAmount.addTextChangedListener(new SimpleTextWatcher(this::updateSaveEnabled));

        b.btnSave.setOnClickListener(v -> {
            if (!b.btnSave.isEnabled()) return;
            vm.amount.setValue(b.etAmount.getText().toString().replaceAll("[^0-9]", ""));
            vm.submit();
        });

        vm.done.observe(getViewLifecycleOwner(), ok -> {
            if (Boolean.TRUE.equals(ok)) {
                Toast.makeText(requireContext(), "Thêm thành công", Toast.LENGTH_SHORT).show();
                resetForm(); // ở lại màn hình thêm
                vm.done.setValue(false);
            }
        });

        // mặc định
        if (vm.date.getValue() == null) vm.date.setValue(LocalDate.now());
        if (vm.type.getValue() == null) vm.type.setValue(TxType.EXPENSE);
        selectMethod("Tiền mặt");
        updateSaveEnabled();
        bindEnableSave();

        return b.getRoot();
    }

    private void setDetailsVisible(boolean visible) {
        b.layoutDetail.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void showDatePicker() {
        LocalDate d = vm.date.getValue() == null ? LocalDate.now() : vm.date.getValue();
        Calendar cal = Calendar.getInstance();
        cal.set(d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth());
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) ->
                        vm.date.setValue(LocalDate.of(year, month + 1, dayOfMonth)),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void selectMethod(String method) {
        vm.method.setValue(method);
        b.optCash.setBackgroundResource(R.drawable.bg_card_neutral_50);
        b.optTransfer.setBackgroundResource(R.drawable.bg_card_neutral_50);
        if ("Tiền mặt".equals(method)) b.optCash.setBackgroundResource(R.drawable.bg_card_primary_5);
        if ("Chuyển khoản".equals(method)) b.optTransfer.setBackgroundResource(R.drawable.bg_card_primary_5);
        updateSaveEnabled();
    }

    private void openTypeSheet() {
        BottomSheetDialog d = new BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialog);
        View sheet = LayoutInflater.from(getContext()).inflate(R.layout.sheet_pick_type, null, false);
        androidx.recyclerview.widget.RecyclerView rv = sheet.findViewById(R.id.rvTypes);

        List<TypePickerAdapter.TypeItem> items = Arrays.asList(
                new TypePickerAdapter.TypeItem("Chi tiêu", R.drawable.ic_expense, R.drawable.bg_icon_round_accent_1),
                new TypePickerAdapter.TypeItem("Thu nhập", R.drawable.ic_income, R.drawable.bg_icon_round_success_1),
                new TypePickerAdapter.TypeItem("Đi vay", R.drawable.ic_cat_borrow, R.drawable.bg_icon_round_primary_1),
                new TypePickerAdapter.TypeItem("Cho vay", R.drawable.ic_cat_lend, R.drawable.bg_icon_round_success_1),
                new TypePickerAdapter.TypeItem("Điều chỉnh số dư", R.drawable.ic_cat_adjust, R.drawable.bg_icon_round_accent_1)
        );

        int selected = vm.type.getValue() == TxType.INCOME ? 1 : 0;

        TypePickerAdapter ad = new TypePickerAdapter(items, selected, (item, pos) -> {
            vm.type.setValue("Thu nhập".equals(item.name) ? TxType.INCOME : TxType.EXPENSE);
            d.dismiss();
        });

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(ad);

        d.setContentView(sheet);
        d.setOnShowListener(dialog -> {
            View bottom = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottom != null) {
                BottomSheetBehavior<View> be = BottomSheetBehavior.from(bottom);
                be.setSkipCollapsed(true);
                be.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        d.show();
    }

    private void openCategorySheet() {
        BottomSheetDialog d = new BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialog);
        View sheet = LayoutInflater.from(getContext()).inflate(R.layout.sheet_pick_category, null, false);

        // click các danh mục có sẵn
        View.OnClickListener pick = v -> {
            int id = v.getId();
            String name;
            if (id == R.id.cat_food) name = "Ăn uống";
            else if (id == R.id.cat_coffee) name = "Cà phê";
            else if (id == R.id.cat_groceries_market) name = "Đi chợ/Siêu thị";
            else if (id == R.id.cat_electric) name = "Điện";
            else if (id == R.id.cat_water) name = "Nước";
            else if (id == R.id.cat_internet) name = "Internet";
            else if (id == R.id.cat_transport) name = "Di chuyển";
            else if (id == R.id.cat_tv) name = "TV";
            else if (id == R.id.cat_gas) name = "GAS";
            else if (id == R.id.cat_rent) name = "Thuê nhà";
            else if (id == R.id.cat_phone) name = "Điện thoại";
            else if (id == R.id.cat_study) name = "Học tập";
            else if (id == R.id.cat_health) name = "Khám sức khỏe";
            else if (id == R.id.cat_vehicle) name = "Bảo dưỡng xe";
            else if (id == R.id.cat_insurance) name = "Bảo hiểm";
            else if (id == R.id.cat_sport) name = "Thể thao";
            else if (id == R.id.cat_music) name = "Nhạc";
            else if (id == R.id.cat_travel) name = "Du lịch";
            else if (id == R.id.cat_games) name = "Trò chơi điện tử";
            else name = "Khác";

            vm.category.setValue(new Category(name));
            d.dismiss();
        };

        int[] ids = {
                R.id.cat_food, R.id.cat_coffee, R.id.cat_groceries_market,
                R.id.cat_electric, R.id.cat_water, R.id.cat_internet, R.id.cat_transport,
                R.id.cat_tv, R.id.cat_gas, R.id.cat_rent, R.id.cat_phone,
                R.id.cat_study, R.id.cat_health, R.id.cat_vehicle, R.id.cat_insurance,
                R.id.cat_sport, R.id.cat_music, R.id.cat_travel, R.id.cat_games
        };
        for (int id : ids) {
            View v = sheet.findViewById(id);
            if (v != null) v.setOnClickListener(pick);
        }

        // nút "Thêm danh mục"
        View btnAdd = sheet.findViewById(R.id.btnAddCategory);
        if (btnAdd != null) btnAdd.setOnClickListener(v -> {
            d.dismiss();
            openCreateCategorySheet();
        });

        // tìm kiếm
        EditText et = sheet.findViewById(R.id.etSearchCategory);
        if (et != null) {
            et.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void onTextChanged(CharSequence s, int st, int b1, int c) {
                    String q = s.toString().trim().toLowerCase();
                    for (int id : ids) {
                        View chip = sheet.findViewById(id);
                        if (chip == null) continue;
                        TextView label = null;
                        if (chip instanceof ViewGroup) {
                            ViewGroup vg = (ViewGroup) chip;
                            View tvCandidate = vg.getChildAt(vg.getChildCount() - 1);
                            if (tvCandidate instanceof TextView) label = (TextView) tvCandidate;
                        }
                        boolean match = true;
                        if (label != null) match = label.getText().toString().toLowerCase().contains(q);
                        chip.setVisibility(match ? View.VISIBLE : View.GONE);
                    }
                }
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

        d.setContentView(sheet);
        d.setOnShowListener(dialog -> {
            View bottom = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottom != null) {
                ViewGroup.LayoutParams lp = bottom.getLayoutParams();
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottom.setLayoutParams(lp);
                BottomSheetBehavior<View> be = BottomSheetBehavior.from(bottom);
                be.setSkipCollapsed(true);
                be.setState(BottomSheetBehavior.STATE_EXPANDED);
                be.setDraggable(true);
            }
        });

        d.show();
    }

    // ====== Tạo danh mục mới: tên + loại 2 hàng + màu + 8 icon (2 hàng) ======
    private void openCreateCategorySheet() {
        BottomSheetDialog d = new BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialog);
        View sheet = LayoutInflater.from(getContext()).inflate(R.layout.sheet_new_category, null, false);

        EditText etName = sheet.findViewById(R.id.etName);

        // 2 nhóm RadioGroup cho 2 hàng
        RadioGroup g1 = sheet.findViewById(R.id.groupType1);
        RadioGroup g2 = sheet.findViewById(R.id.groupType2);

        // ==== chọn màu: 3 ô ví dụ (c1, c2, c3) ====
        int[] colorViews = { R.id.c1, R.id.c2, R.id.c3 };
        // bạn có thể lưu hẳn mã màu, ở đây demo giữ id resource nền tròn
        final int[] selColorIdx = {0};
        View.OnClickListener pickColor = v -> {
            for (int i = 0; i < colorViews.length; i++) {
                View cv = sheet.findViewById(colorViews[i]);
                boolean sel = v.getId() == colorViews[i];
                cv.setAlpha(sel ? 1f : 0.35f);
                if (sel) selColorIdx[0] = i;
            }
        };
        for (int id : colorViews) sheet.findViewById(id).setOnClickListener(pickColor);
        pickColor.onClick(sheet.findViewById(R.id.c1));

        // ==== chọn icon: 8 cái, chia 2 hàng trong XML ====
        int[] iconViews = {
                R.id.i1, R.id.i2, R.id.i3, R.id.i4,
                R.id.i5, R.id.i6, R.id.i7, R.id.i8
        };
        int[] iconRes = {
                R.drawable.ic_cat_food, R.drawable.ic_cat_coffee, R.drawable.ic_cat_groceries, R.drawable.ic_cat_health,
                R.drawable.ic_cat_phone, R.drawable.ic_cat_music, R.drawable.ic_cat_travel, R.drawable.ic_cat_sport
        };
        final int[] selIconIdx = {0};
        View.OnClickListener pickIcon = v -> {
            for (int i = 0; i < iconViews.length; i++) {
                View iv = sheet.findViewById(iconViews[i]);
                boolean sel = v.getId() == iconViews[i];
                iv.setAlpha(sel ? 1f : 0.35f);
                if (sel) selIconIdx[0] = i;
            }
        };
        for (int id : iconViews) sheet.findViewById(id).setOnClickListener(pickIcon);
        pickIcon.onClick(sheet.findViewById(R.id.i1));

        // Tạo
        sheet.findViewById(R.id.btnCreate).setOnClickListener(v -> {
            String name = etName.getText() == null ? "" : etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Nhập tên danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            int checkedId = g1.getCheckedRadioButtonId() != -1
                    ? g1.getCheckedRadioButtonId() : g2.getCheckedRadioButtonId();
            String group;
            if (checkedId == R.id.rMonthly) group = "Chi tiêu hằng tháng";
            else if (checkedId == R.id.rEssential) group = "Chi tiêu cần thiết";
            else if (checkedId == R.id.rEntertainment) group = "Vui chơi giải trí";
            else group = "Chi tiêu hằng ngày";

            // Map màu đơn giản theo index (tự mở rộng theo nhu cầu)
            int colorTag;
            if (selColorIdx[0] == 1) colorTag = R.color.accent_1;
            else if (selColorIdx[0] == 2) colorTag = R.color.success_1;
            else colorTag = R.color.primary_1;

            // Category mở rộng: name, color, icon, group/type
            Category cat = new Category(name);
            // nếu Category có các field, set thêm ở đây:
            // cat.colorRes = colorTag;
            // cat.iconRes = iconRes[selIconIdx[0]];
            // cat.group = group;

            // nếu ViewModel có lưu DS category, thêm vào:
            // vm.addCategory(cat);

            vm.category.setValue(cat);
            Toast.makeText(requireContext(), "Đã tạo danh mục", Toast.LENGTH_SHORT).show();
            d.dismiss();
        });

        d.setContentView(sheet);
        d.show();
    }

    private void updateSaveEnabled() {
        boolean hasAmount = b.etAmount.getText() != null
                && b.etAmount.getText().toString().replaceAll("[^0-9]", "").length() > 0;
        boolean hasCat = vm.category.getValue() != null;
        boolean hasType = vm.type.getValue() != null;
        boolean hasMethod = vm.method.getValue() != null && !vm.method.getValue().isEmpty();
        boolean ok = hasAmount && hasCat && hasType && hasMethod;
        b.btnSave.setEnabled(ok);
        b.btnSave.setAlpha(ok ? 1f : 0.5f);
    }

    private void bindEnableSave() {
        androidx.lifecycle.Observer<Object> o = x -> updateSaveEnabled();
        vm.category.observe(getViewLifecycleOwner(), o);
        vm.date.observe(getViewLifecycleOwner(), o);
        b.etAmount.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int st, int b1, int c) { o.onChanged(null); }
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void resetForm() {
        b.etAmount.setText("");
        vm.type.setValue(TxType.EXPENSE);
        vm.category.setValue(null);
        vm.method.setValue("Tiền mặt");
        vm.date.setValue(LocalDate.now());
        setDetailsVisible(false);
        b.btnToggleDetail.setText("Hiển thị chi tiết ⌄");
        b.btnToggleDetail.setBackgroundResource(R.drawable.bg_card_neutral_50);
        updateSaveEnabled();
        b.scrollView.fullScroll(View.FOCUS_UP);
    }

    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final Runnable onChange;
        SimpleTextWatcher(Runnable r) { onChange = r; }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        public void afterTextChanged(android.text.Editable s) { onChange.run(); }
    }
}
