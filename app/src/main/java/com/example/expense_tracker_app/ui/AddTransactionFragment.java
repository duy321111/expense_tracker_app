package com.example.expense_tracker_app.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
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
    // Bỏ TX_TYPES_DISPLAY nếu dùng RadioGroup
    // private final String[] TX_TYPES_DISPLAY = {"Chi tiêu", "Thu nhập", "Đi vay", "Cho vay", "Điều chỉnh số dư"};

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s){
        b = FragmentAddBinding.inflate(i, c, false);
        vm = new ViewModelProvider(this).get(AddTxViewModel.class);

        // KHẮC PHỤC LỖI: LOGIC RADIO GROUP (rgType) thay thế spTxType

        // 1. Loại giao dịch (RadioGroup rgType)
        // Đây là code mẫu cho RadioGroup (Cần đảm bảo rgType, rbIncome, rbExpense tồn tại trong fragment_add.xml)
        if (b.getRoot().findViewById(R.id.rgType) != null) {
            // Mặc định: Chi tiêu (EXPENSE)
            if (b.getRoot().findViewById(R.id.rbExpense) != null) {
                b.rgType.check(R.id.rbExpense);
                vm.type.setValue(TxType.EXPENSE);
            }

            b.rgType.setOnCheckedChangeListener((group, checkedId) -> {
                vm.type.setValue(checkedId == R.id.rbIncome ? TxType.INCOME : TxType.EXPENSE);
                // Khởi tạo lại section chi tiết nếu đã mở
                if (b.sectionDetails.getVisibility() == View.VISIBLE) {
                    showDetailSection(null);
                }
            });
        }


        // 2. Chọn hình thức thanh toán (spMethod)
        b.spMethod.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, Arrays.asList("Tiền mặt","Chuyển khoản","ATM")));
        b.spMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                vm.method.setValue(parent.getItemAtPosition(pos).toString());
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 3. Tiếp tục -> mở sheet chọn danh mục (btnContinue)
        b.btnContinue.setOnClickListener(v -> {
            if (vm.isSelectableCategoryType()){
                openCategorySheet();
            } else {
                showDetailSection(null);
            }
        });

        // 4. Bỏ qua -> vẫn mở phần chi tiết (tvSkip)
        b.tvSkip.setOnClickListener(v -> showDetailSection(null));

        // 5. Chọn ngày (btnDate)
        vm.date.observe(getViewLifecycleOwner(), date -> {
            b.btnDate.setText(String.format("%02d/%02d/%d",
                    date.getDayOfMonth(), date.getMonthValue(), date.getYear()));
        });
        b.btnDate.setOnClickListener(v -> {
            LocalDate d = vm.date.getValue();
            if (d == null) d = LocalDate.now();

            Calendar cal = Calendar.getInstance();
            cal.set(d.getYear(), d.getMonthValue()-1, d.getDayOfMonth());
            new DatePickerDialog(requireContext(), (view, y, m, day)-> {
                vm.date.setValue(LocalDate.of(y, m+1, day));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // 6. Submit (btnSubmit)
        b.btnSubmit.setOnClickListener(v -> {
            vm.amount.setValue(b.etAmount.getText().toString().replaceAll("[^0-9]",""));
            vm.note.setValue(b.etNote.getText().toString());
            vm.submit();
        });

        // 7. Xử lý sau khi hoàn tất
        vm.done.observe(getViewLifecycleOwner(), ok -> {
            if(Boolean.TRUE.equals(ok)){
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Thêm thành công").setMessage("Giao dịch đã được lưu")
                        .setPositiveButton("Xác nhận",(d1,which)-> d1.dismiss()).show();
                vm.done.setValue(false);
            }
        });

        // 8. Cập nhật UI theo loại giao dịch
        vm.type.observe(getViewLifecycleOwner(), type -> {
            b.btnContinue.setText(vm.isSelectableCategoryType() ? "Tiếp tục" : "Thêm chi tiết");
        });

        vm.date.setValue(LocalDate.now());

        return b.getRoot();
    }

    private void openCategorySheet(){
        BottomSheetDialog d = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(getContext()).inflate(R.layout.sheet_pick_category, null, false);

        View.OnClickListener pick = v -> {
            String name;
            int id = v.getId();

            if (id == R.id.cat_food) name = "Ăn uống";
            else if (id == R.id.cat_coffee) name = "Cà phê";
                // Dùng ID đã gộp
            else if (id == R.id.cat_groceries_market) name = "Đi chợ/Siêu thị";

            else if (id == R.id.cat_electric) name = "Điện";
            else if (id == R.id.cat_water) name = "Nước";
            else if (id == R.id.cat_internet) name = "Internet";
            else if (id == R.id.cat_transport) name = "Di chuyển";

            else if (id == R.id.cat_salary) name = "Tiền lương";
            else if (id == R.id.cat_sale) name = "Bán hàng";
            else if (id == R.id.cat_debt_return) name = "Được trả nợ";
            else name = "Khác";

            Category c = new Category(name);
            vm.category.setValue(c);
            showDetailSection(c.name);
            d.dismiss();
        };

        // Cập nhật mảng ID
        int[] ids = { R.id.cat_food, R.id.cat_coffee, R.id.cat_groceries_market,
                R.id.cat_electric, R.id.cat_water, R.id.cat_internet, R.id.cat_transport,
                R.id.cat_salary, R.id.cat_sale, R.id.cat_debt_return };

        for(int id: ids) {
            View categoryView = sheet.findViewById(id);
            if(categoryView != null) categoryView.setOnClickListener(pick);
        }

        d.setContentView(sheet); d.show();
    }

    private void showDetailSection(String picked){
        b.sectionInit.setVisibility(View.GONE);
        b.sectionDetails.setVisibility(View.VISIBLE);

        if(vm.isSelectableCategoryType()){
            b.btnCategory.setVisibility(View.VISIBLE);
            
            if (picked != null) b.btnCategory.setText(picked);
            else b.btnCategory.setText("Chọn danh mục");
            b.btnCategory.setOnClickListener(v -> openCategorySheet());
        } else {
            b.btnCategory.setVisibility(View.GONE);
            vm.category.setValue(new Category(vm.type.getValue().name()));
        }
    }
}