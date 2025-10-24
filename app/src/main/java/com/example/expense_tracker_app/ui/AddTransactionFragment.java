package com.example.expense_tracker_app.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.ArrayAdapter;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Category;
import com.example.expense_tracker_app.data.model.TxType;
import com.example.expense_tracker_app.databinding.FragmentAddBinding;
import com.example.expense_tracker_app.viewmodel.AddTxViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;

public class AddTransactionFragment extends Fragment {
    private FragmentAddBinding b; private AddTxViewModel vm;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s){
        b = FragmentAddBinding.inflate(i, c, false);
        vm = new ViewModelProvider(this).get(AddTxViewModel.class);

        // default: Chi tiêu
        b.rbExpense.setChecked(true);
        b.rgType.setOnCheckedChangeListener((g, id)->
                vm.type.setValue(id==R.id.rbIncome? TxType.INCOME: TxType.EXPENSE));

        // Tiếp tục -> mở sheet chọn danh mục
        b.btnContinue.setOnClickListener(v -> openCategorySheet());

        // Bỏ qua -> vẫn mở phần chi tiết
        b.tvSkip.setOnClickListener(v -> showDetailSection(null));

        // Phương thức thanh toán
        b.spMethod.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, Arrays.asList("Tiền mặt","Chuyển khoản","ATM")));

        b.btnDate.setOnClickListener(v -> {
            LocalDate d = vm.date.getValue(); Calendar cal = Calendar.getInstance();
            cal.set(d.getYear(), d.getMonthValue()-1, d.getDayOfMonth());
            new DatePickerDialog(requireContext(), (view, y, m, day)-> {
                vm.date.setValue(LocalDate.of(y, m+1, day));
                b.btnDate.setText(day + "/" + (m+1) + "/" + y);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        b.btnSubmit.setOnClickListener(v -> {
            vm.amount.setValue(b.etAmount.getText().toString().replaceAll("[^0-9]",""));
            vm.note.setValue(b.etNote.getText().toString());
            vm.submit();
        });

        vm.done.observe(getViewLifecycleOwner(), ok -> {
            if(Boolean.TRUE.equals(ok)){
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Thêm thành công").setMessage("Giao dịch đã được lưu")
                        .setPositiveButton("Xác nhận",(d1,which)-> d1.dismiss()).show();
                vm.done.setValue(false);
            }
        });

        return b.getRoot();
    }

    private void openCategorySheet(){
        BottomSheetDialog d = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(getContext()).inflate(R.layout.sheet_pick_category, null, false);

        View.OnClickListener pick = v -> {
            String name;
            switch (v.getId()){
                case R.id.cat_food:        name="Ăn uống"; break;
                case R.id.cat_coffee:      name="Cà phê"; break;
                case R.id.cat_groceries:   name="Đi chợ"; break;
                case R.id.cat_market:      name="Siêu thị"; break;
                case R.id.cat_electric:    name="Điện"; break;
                case R.id.cat_water:       name="Nước"; break;
                case R.id.cat_internet:    name="Internet"; break;
                case R.id.cat_transport:   name="Di chuyển"; break;
                case R.id.cat_salary:      name="Lương"; break;
                case R.id.cat_sale:        name="Bán hàng"; break;
                case R.id.cat_debt_return: name="Được trả nợ"; break;
                default: name="Khác";
            }
            Category c = new Category(name);
            vm.category.setValue(c);
            showDetailSection(c.name);
            d.dismiss();
        };

        int[] ids = { R.id.cat_food, R.id.cat_coffee, R.id.cat_groceries, R.id.cat_market,
                R.id.cat_electric, R.id.cat_water, R.id.cat_internet, R.id.cat_transport,
                R.id.cat_salary, R.id.cat_sale, R.id.cat_debt_return };

        for(int id: ids) sheet.findViewById(id).setOnClickListener(pick);
        d.setContentView(sheet); d.show();
    }

    private void showDetailSection(String picked){
        b.sectionDetails.setVisibility(View.VISIBLE);
        if (picked != null) b.btnCategory.setText(picked);
        else b.btnCategory.setText("Chọn danh mục");
        // Cho phép mở lại sheet khi bấm nút danh mục
        b.btnCategory.setOnClickListener(v -> openCategorySheet());
    }
}
