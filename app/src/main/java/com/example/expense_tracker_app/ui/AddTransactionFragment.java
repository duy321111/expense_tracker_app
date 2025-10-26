package com.example.expense_tracker_app.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        b = FragmentAddBinding.inflate(i, c, false);
        vm = new ViewModelProvider(this).get(AddTxViewModel.class);

        // Nút đóng
        b.btnClose.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Mở chọn loại giao dịch
        View.OnClickListener openType = v -> openTypeSheet();
        b.rowType.setOnClickListener(openType);
        b.ivType.setOnClickListener(openType);

        // Mở chọn danh mục
        View.OnClickListener openCat = v -> openCategorySheet();
        b.rowCategory.setOnClickListener(openCat);
        b.ivCategory.setOnClickListener(openCat);

        // Chọn ngày
        b.rowDate.setOnClickListener(v -> showDatePicker());

        // Chọn phương thức
        b.optCash.setOnClickListener(v -> selectMethod("Tiền mặt"));
        b.optTransfer.setOnClickListener(v -> selectMethod("Chuyển khoản"));
        selectMethod(vm.method.getValue());

        // Quan sát LiveData
        vm.date.observe(getViewLifecycleOwner(), d ->
                b.tvDate.setText(String.format("%02d/%02d/%d",
                        d.getDayOfMonth(), d.getMonthValue(), d.getYear())));

        vm.type.observe(getViewLifecycleOwner(), t -> {
            String txt = t == TxType.INCOME ? "Thu nhập" : "Chi tiêu";
            b.tvType.setText(txt);
        });

        vm.category.observe(getViewLifecycleOwner(), cata -> {
            if (cata != null) {
                b.tvCategory.setText(cata.name);
                b.tvCategory.setTextColor(requireContext().getColor(R.color.primary_1));
            } else {
                b.tvCategory.setText("Danh mục mới");
                b.tvCategory.setTextColor(requireContext().getColor(R.color.neutral_700));
            }
            updateSaveEnabled();
        });

        // Nhập số tiền
        b.etAmount.addTextChangedListener(new SimpleTextWatcher(this::updateSaveEnabled));

        // Nút lưu
        b.btnSave.setOnClickListener(v -> {
            if (!b.btnSave.isEnabled()) return;
            vm.amount.setValue(b.etAmount.getText().toString().replaceAll("[^0-9]", ""));
            vm.submit();
        });

        vm.done.observe(getViewLifecycleOwner(), ok -> {
            if (Boolean.TRUE.equals(ok)) {
                Toast.makeText(requireContext(), "Thêm thành công", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
                vm.done.setValue(false);
            }
        });

        // Khởi tạo mặc định
        if (vm.date.getValue() == null) vm.date.setValue(LocalDate.now());
        if (vm.type.getValue() == null) vm.type.setValue(TxType.EXPENSE);
        updateSaveEnabled();
        bindEnableSave();

        return b.getRoot();
    }

    // ------------------- HÀM PHỤ -------------------

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
        if ("Tiền mặt".equals(method))
            b.optCash.setBackgroundResource(R.drawable.bg_card_primary_5);
        if ("Chuyển khoản".equals(method))
            b.optTransfer.setBackgroundResource(R.drawable.bg_card_primary_5);
        updateSaveEnabled();
    }

    private void openTypeSheet() {
        BottomSheetDialog d = new BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialog);
        View sheet = LayoutInflater.from(getContext()).inflate(R.layout.sheet_pick_type, null, false);
        androidx.recyclerview.widget.RecyclerView rv = sheet.findViewById(R.id.rvTypes);

        List<TypePickerAdapter.TypeItem> items = Arrays.asList(
                new TypePickerAdapter.TypeItem("Chi tiêu", R.drawable.ic_expense, R.drawable.bg_icon_round_accent_1),
                new TypePickerAdapter.TypeItem("Thu nhập", R.drawable.ic_income, R.drawable.bg_icon_round_success_1),
                new TypePickerAdapter.TypeItem("Đi vay", R.drawable.ic_cat_borrow, R.drawable.bg_icon_round_primary_2),
                new TypePickerAdapter.TypeItem("Cho vay", R.drawable.ic_cat_lend, R.drawable.bg_icon_round_primary_2),
                new TypePickerAdapter.TypeItem("Điều chỉnh số dư", R.drawable.ic_cat_adjust, R.drawable.bg_icon_round_primary_3)
        );

        int selected = 0;
        if (vm.type.getValue() == TxType.INCOME) selected = 1;
        else if (vm.type.getValue() == TxType.BORROW) selected = 2;
        else if (vm.type.getValue() == TxType.LEND) selected = 3;
        else if (vm.type.getValue() == TxType.ADJUST) selected = 4;

        TypePickerAdapter ad = new TypePickerAdapter(items, selected, (item, pos) -> {
            TxType picked =
                    "Thu nhập".equals(item.name) ? TxType.INCOME :
                            "Đi vay".equals(item.name) ? TxType.BORROW :
                                    "Cho vay".equals(item.name) ? TxType.LEND :
                                            "Điều chỉnh số dư".equals(item.name) ? TxType.ADJUST :
                                                    TxType.EXPENSE;
            vm.type.setValue(picked);
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

        View.OnClickListener pick = v -> {
            String name;
            int id = v.getId();
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
            else if (id == R.id.cat_music) name = "Nhạc";
            else if (id == R.id.cat_travel) name = "Du lịch";
            else if (id == R.id.cat_sport) name = "Thể thao";
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
                R.id.cat_music, R.id.cat_travel, R.id.cat_sport, R.id.cat_games
        };
        for (int id : ids) {
            View v = sheet.findViewById(id);
            if (v != null) v.setOnClickListener(pick);
        }

        // Nút tạo danh mục mới
        View btnAdd = sheet.findViewById(R.id.btnAddCategory);
        if (btnAdd != null) btnAdd.setOnClickListener(v -> {
            d.dismiss();
            openCreateCategorySheet();
        });

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

    private void openCreateCategorySheet() {
        BottomSheetDialog dlg = new BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialog);
        View v = LayoutInflater.from(getContext()).inflate(R.layout.sheet_new_category, null, false);
        if (v == null) return;

        final int[] pickedIcon = {R.drawable.ic_cat_food};
        final int[] pickedBg = {R.drawable.bg_icon_round_primary_1};

        View i1 = v.findViewById(R.id.i1);
        View i2 = v.findViewById(R.id.i2);
        View i3 = v.findViewById(R.id.i3);
        if (i1 != null) i1.setOnClickListener(iv -> pickedIcon[0] = R.drawable.ic_cat_food);
        if (i2 != null) i2.setOnClickListener(iv -> pickedIcon[0] = R.drawable.ic_cat_coffee);
        if (i3 != null) i3.setOnClickListener(iv -> pickedIcon[0] = R.drawable.ic_cat_groceries);

        View c1 = v.findViewById(R.id.c1);
        View c2 = v.findViewById(R.id.c2);
        View c3 = v.findViewById(R.id.c3);
        if (c1 != null) c1.setOnClickListener(c -> pickedBg[0] = R.drawable.bg_icon_round_primary_1);
        if (c2 != null) c2.setOnClickListener(c -> pickedBg[0] = R.drawable.bg_icon_round_accent_1);
        if (c3 != null) c3.setOnClickListener(c -> pickedBg[0] = R.drawable.bg_icon_round_success_1);

        View btnCreate = v.findViewById(R.id.btnCreate);
        if (btnCreate != null) btnCreate.setOnClickListener(btn -> {
            String name = ((android.widget.EditText) v.findViewById(R.id.etName)).getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Nhập tên danh mục", Toast.LENGTH_SHORT).show();
                return;
            }
            Category c = new Category(name);
            vm.category.setValue(c);
            b.tvCategory.setText(c.name);
            dlg.dismiss();
        });

        dlg.setContentView(v);
        dlg.setOnShowListener(dialog -> {
            View bottom = dlg.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottom != null) {
                BottomSheetBehavior<View> be = BottomSheetBehavior.from(bottom);
                be.setSkipCollapsed(true);
                be.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        dlg.show();
    }

    private void updateSaveEnabled() {
        boolean hasAmount = b.etAmount.getText() != null
                && b.etAmount.getText().toString().replaceAll("[^0-9]", "").length() > 0;
        boolean hasCat = vm.category.getValue() != null;
        boolean ok = hasAmount && hasCat;
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

    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final Runnable onChange;
        SimpleTextWatcher(Runnable r) { onChange = r; }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        public void afterTextChanged(android.text.Editable s) { onChange.run(); }
    }
}
