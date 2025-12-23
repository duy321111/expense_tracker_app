package com.example.expense_tracker_app.ui;

import android.graphics.Color;
import android.view.Gravity;
import android.widget.GridLayout;
import android.content.Context;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.net.Uri;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Category;
import com.example.expense_tracker_app.data.model.TxType;
import com.example.expense_tracker_app.viewmodel.AddTxViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class AddTransactionFragment extends Fragment {

    private AddTxViewModel viewModel;

    // Views
    private EditText etAmount;
    private LinearLayout rowType, rowCategory, rowDate;
    private TextView tvType, tvCategory, tvDate;
    private LinearLayout optCash, optTransfer;
    private Button btnSave;
    private View toolbar;

    // Views Chi tiết
    private TextView btnToggleDetail;
    private LinearLayout layoutDetail;
    private LinearLayout rowNote, rowLocation, rowImage;
    private SwitchCompat switchReport;
    private TextView tvReportHint;
    private TextView tvImageStatus;

    private ActivityResultLauncher<String> pickImageLauncher;

    private final List<String> AVAILABLE_ICONS = Arrays.asList(
            "ic_cat_food", "ic_cat_coffee", "ic_cat_groceries",
            "ic_cat_transport", "ic_cat_home",
            "ic_cat_health", "ic_cat_car_service", "ic_cat_insurance",
            "ic_cat_sport", "ic_cat_music", "ic_cat_travel", "ic_cat_gamepad"
    );
    private String selectedIconName = "ic_cat_food";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);
        viewModel = new ViewModelProvider(this).get(AddTxViewModel.class);

        initViews(view);
        registerImagePicker();
        setupEvents();
        observeViewModel();
        return view;
    }

    private void registerImagePicker() {
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                String internalPath = saveImageToInternalStorage(uri);
                if (internalPath != null) {
                    viewModel.imagePath.setValue(internalPath);
                    Toast.makeText(getContext(), "Đã lưu ảnh!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Lỗi khi lưu ảnh", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String saveImageToInternalStorage(Uri sourceUri) {
        try {
            String fileName = "img_" + System.currentTimeMillis() + ".jpg";
            File file = new File(requireContext().getFilesDir(), fileName);
            InputStream inputStream = requireContext().getContentResolver().openInputStream(sourceUri);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, length);
            outputStream.close();
            inputStream.close();
            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        etAmount = view.findViewById(R.id.etAmount);
        rowType = view.findViewById(R.id.row_type);
        tvType = view.findViewById(R.id.tv_type);
        rowCategory = view.findViewById(R.id.row_category);
        tvCategory = view.findViewById(R.id.tv_category);
        rowDate = view.findViewById(R.id.row_date);
        tvDate = view.findViewById(R.id.tv_date);
        optCash = view.findViewById(R.id.opt_cash);
        optTransfer = view.findViewById(R.id.opt_transfer);
        btnSave = view.findViewById(R.id.btnSave);
        btnToggleDetail = view.findViewById(R.id.btnToggleDetail);
        layoutDetail = view.findViewById(R.id.layout_detail);
        rowNote = view.findViewById(R.id.row_note);
        rowLocation = view.findViewById(R.id.row_location);
        rowImage = view.findViewById(R.id.row_image);
        switchReport = view.findViewById(R.id.switchReport);
        tvReportHint = view.findViewById(R.id.tv_report_hint);

        if (rowImage != null && rowImage.getChildCount() > 0) {
            for (int i = 0; i < rowImage.getChildCount(); i++) {
                View child = rowImage.getChildAt(i);
                if (child instanceof TextView) {
                    tvImageStatus = (TextView) child;
                    break;
                }
            }
        }

        // --- SỬA LOGIC KHỞI TẠO: Set trạng thái ban đầu của nút Save ---
        // Mặc định ban đầu chưa có tiền -> Nút mờ đi (alpha 0.5) nhưng VẪN CLICK ĐƯỢC
        btnSave.setAlpha(0.5f);
        btnSave.setEnabled(true);
    }

    private void setupEvents() {
        if (toolbar != null) toolbar.setOnClickListener(v -> requireActivity().onBackPressed());

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.amount.setValue(s.toString());
                boolean hasMoney = s.length() > 0;

                // --- SỬA LOGIC Ở ĐÂY ---
                // Luôn cho phép bấm nút (setEnabled = true)
                btnSave.setEnabled(true);
                // Chỉ thay đổi độ mờ để báo hiệu trạng thái
                btnSave.setAlpha(hasMoney ? 1.0f : 0.5f);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        rowType.setOnClickListener(v -> showTypePickerDialog());
        rowCategory.setOnClickListener(v -> showCategoryPickerDialog());
        rowDate.setOnClickListener(v -> showDatePicker());

        optCash.setOnClickListener(v -> {
            viewModel.method.setValue("Tiền mặt");
            updateMethodUI(true);
        });

        optTransfer.setOnClickListener(v -> {
            viewModel.method.setValue("Chuyển khoản");
            updateMethodUI(false);
        });

        btnToggleDetail.setOnClickListener(v -> {
            if (layoutDetail.getVisibility() == View.VISIBLE) {
                layoutDetail.setVisibility(View.GONE);
                btnToggleDetail.setText("Chi tiết");
            } else {
                layoutDetail.setVisibility(View.VISIBLE);
                btnToggleDetail.setText("Ẩn chi tiết");
            }
        });

        rowNote.setOnClickListener(v -> {
            EditText input = new EditText(getContext());
            input.setHint("Nhập ghi chú...");
            input.setText(viewModel.note.getValue());
            new AlertDialog.Builder(getContext()).setTitle("Ghi chú").setView(input)
                    .setPositiveButton("OK", (d, w) -> viewModel.note.setValue(input.getText().toString()))
                    .setNegativeButton("Hủy", null).show();
        });

        rowLocation.setOnClickListener(v -> {
            EditText input = new EditText(getContext());
            input.setHint("Nhập địa điểm...");
            input.setText(viewModel.location.getValue());
            new AlertDialog.Builder(getContext()).setTitle("Địa điểm").setView(input)
                    .setPositiveButton("OK", (d, w) -> viewModel.location.setValue(input.getText().toString()))
                    .setNegativeButton("Hủy", null).show();
        });

        rowImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        switchReport.setOnCheckedChangeListener((btn, isChecked) -> {
            viewModel.excludeReport.setValue(isChecked);
            tvReportHint.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if(isChecked) tvReportHint.setText("Giao dịch này sẽ không tính vào báo cáo.");
        });

        // --- SỬA LOGIC CLICK NÚT LƯU ---
        btnSave.setOnClickListener(v -> {
            // Kiểm tra xem đã nhập tiền chưa
            String currentAmount = etAmount.getText().toString();
            if (currentAmount.isEmpty()) {
                // Nếu chưa nhập -> Hiện thông báo
                Toast.makeText(getContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            } else {
                // Nếu đã nhập -> Thực hiện lưu
                viewModel.submit();
            }
        });
    }

    private void observeViewModel() {
        viewModel.date.observe(getViewLifecycleOwner(), d -> tvDate.setText(d.getDayOfMonth() + "/" + d.getMonthValue() + "/" + d.getYear()));

        viewModel.type.observe(getViewLifecycleOwner(), t -> {
            String s = "Chi tiêu";
            if (t == TxType.INCOME) {
                s = "Thu nhập";
            } else if (t == TxType.BORROW) {
                s = "Đi vay";
            } else if (t == TxType.LEND) {
                s = "Cho vay";
            }
            tvType.setText(s);
            tvCategory.setText("Chọn danh mục");
            viewModel.category.setValue(null);
        });

        viewModel.category.observe(getViewLifecycleOwner(), c -> { if (c != null) tvCategory.setText(c.name); });

        viewModel.method.observe(getViewLifecycleOwner(), m -> updateMethodUI("Tiền mặt".equals(m)));

        viewModel.imagePath.observe(getViewLifecycleOwner(), path -> {
            if (tvImageStatus != null) {
                if (path != null && !path.isEmpty()) {
                    tvImageStatus.setText("Đã chọn 1 ảnh");
                    tvImageStatus.setTextColor(getResources().getColor(R.color.primary_1, null));
                } else {
                    tvImageStatus.setText("Thêm hình ảnh");
                    tvImageStatus.setTextColor(getResources().getColor(R.color.neutral_700, null));
                }
            }
        });
        viewModel.done.observe(getViewLifecycleOwner(), done -> {
            if (done) {
                Toast.makeText(getContext(), "Lưu thành công", Toast.LENGTH_SHORT).show();
                viewModel.done.setValue(false);
                if (getActivity() != null) getActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    // ... (Các hàm dialog giữ nguyên) ...

    private void showTypePickerDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(R.layout.sheet_pick_type);
        RecyclerView rv = dialog.findViewById(R.id.rvTypes);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            List<TxType> list = Arrays.asList(TxType.INCOME, TxType.EXPENSE, TxType.BORROW, TxType.LEND);
            rv.setAdapter(new TypeAdapter(requireContext(), list, type -> {
                viewModel.type.setValue(type);
                dialog.dismiss();
            }));
        }
        dialog.show();
    }

    private void showCategoryPickerDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = getLayoutInflater().inflate(R.layout.sheet_pick_category, null);
        dialog.setContentView(sheet);

        bindCat(sheet, dialog, R.id.cat_food, "Ăn uống", "ic_cat_food");
        bindCat(sheet, dialog, R.id.cat_coffee, "Cà phê", "ic_cat_coffee");
        bindCat(sheet, dialog, R.id.cat_groceries_market, "Đi chợ/Siêu thị", "ic_cat_groceries");
        bindCat(sheet, dialog, R.id.cat_electric, "Điện", "ic_cat_electric");
        bindCat(sheet, dialog, R.id.cat_water, "Nước", "ic_cat_water");
        bindCat(sheet, dialog, R.id.cat_internet, "Internet", "ic_cat_internet");
        bindCat(sheet, dialog, R.id.cat_transport, "Di chuyển", "ic_cat_transport");
        bindCat(sheet, dialog, R.id.cat_tv, "TV", "ic_cat_tv");
        bindCat(sheet, dialog, R.id.cat_gas, "GAS", "ic_cat_gas");
        bindCat(sheet, dialog, R.id.cat_rent, "Thuê nhà", "ic_cat_home");
        bindCat(sheet, dialog, R.id.cat_phone, "Điện thoại", "ic_cat_phone");
        bindCat(sheet, dialog, R.id.cat_study, "Học tập", "ic_cat_study");
        bindCat(sheet, dialog, R.id.cat_health, "Khám sức khỏe", "ic_cat_health");
        bindCat(sheet, dialog, R.id.cat_vehicle, "Bảo dưỡng xe", "ic_cat_car_service");
        bindCat(sheet, dialog, R.id.cat_insurance, "Bảo hiểm", "ic_cat_insurance");
        bindCat(sheet, dialog, R.id.cat_sport, "Thể thao", "ic_cat_sport");
        bindCat(sheet, dialog, R.id.cat_music, "Nhạc", "ic_cat_music");
        bindCat(sheet, dialog, R.id.cat_travel, "Du lịch", "ic_cat_travel");
        bindCat(sheet, dialog, R.id.cat_games, "Trò chơi", "ic_cat_gamepad");

        TextView tvOtherTitle = sheet.findViewById(R.id.tvOtherTitle);
        View cardOther = sheet.findViewById(R.id.cardOther);
        GridLayout gridOther = sheet.findViewById(R.id.gridOther);
        Button btnAdd = sheet.findViewById(R.id.btnAddCategory);

        List<Category> customList = viewModel.getCustomCategories();
        if (customList != null && !customList.isEmpty()) {
            tvOtherTitle.setVisibility(View.VISIBLE);
            cardOther.setVisibility(View.VISIBLE);

            for (Category cat : customList) {
                LinearLayout itemLayout = new LinearLayout(getContext());
                itemLayout.setOrientation(LinearLayout.VERTICAL);
                itemLayout.setGravity(Gravity.CENTER);
                itemLayout.setPadding(10, 20, 10, 20);

                FrameLayout iconBackground = new FrameLayout(getContext());
                int bgSize = (int) (50 * getResources().getDisplayMetrics().density); // 50dp
                LinearLayout.LayoutParams bgParams = new LinearLayout.LayoutParams(bgSize, bgSize);
                bgParams.bottomMargin = 8;
                iconBackground.setLayoutParams(bgParams);
                iconBackground.setBackgroundResource(R.drawable.bg_icon_round_accent_1); // Màu nền

                ImageView iv = new ImageView(getContext());
                int resId = getResources().getIdentifier(cat.icon, "drawable", requireContext().getPackageName());
                iv.setImageResource(resId != 0 ? resId : R.drawable.ic_category);
                iv.setColorFilter(Color.WHITE); // Fix: Đổi màu icon thành trắng

                int iconSize = (int) (24 * getResources().getDisplayMetrics().density); // 24dp
                FrameLayout.LayoutParams imgParams = new FrameLayout.LayoutParams(iconSize, iconSize);
                imgParams.gravity = Gravity.CENTER;
                iv.setLayoutParams(imgParams);

                iconBackground.addView(iv);

                TextView tv = new TextView(getContext());
                tv.setText(cat.name);
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(12);
                tv.setMaxLines(2);

                itemLayout.addView(iconBackground);
                itemLayout.addView(tv);

                itemLayout.setOnClickListener(v -> {
                    viewModel.category.setValue(cat);
                    dialog.dismiss();
                });

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                itemLayout.setLayoutParams(params);
                gridOther.addView(itemLayout);
            }
        }

        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> showAddCategoryDialog(dialog));
        }

        dialog.show();
    }

    private void showAddCategoryDialog(BottomSheetDialog parentSheet) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if(dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etName = view.findViewById(R.id.etCatName);
        RecyclerView rvIcons = view.findViewById(R.id.rvIcons);
        Button btnConfirm = view.findViewById(R.id.btnConfirmAdd);

        rvIcons.setLayoutManager(new GridLayoutManager(getContext(), 5));
        IconAdapter iconAdapter = new IconAdapter(AVAILABLE_ICONS, iconName -> {
            selectedIconName = iconName;
        });
        rvIcons.setAdapter(iconAdapter);

        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                viewModel.addNewCategory(name, selectedIconName);
                dialog.dismiss();
                parentSheet.dismiss();
                Toast.makeText(getContext(), "Đã thêm: " + name, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void bindCat(View root, BottomSheetDialog d, int id, String name, String icon) {
        View v = root.findViewById(id);
        if (v != null) {
            v.setOnClickListener(view -> {
                viewModel.category.setValue(new Category(name, icon));
                d.dismiss();
            });
        }
    }

    private void updateMethodUI(boolean isCash) {
        if (optCash == null) return;
        optCash.setBackgroundResource(isCash ? R.drawable.bg_icon_round_neutral_100 : R.drawable.bg_card_neutral_50);
        optTransfer.setBackgroundResource(!isCash ? R.drawable.bg_icon_round_neutral_100 : R.drawable.bg_card_neutral_50);
    }

    private void showDatePicker() {
        LocalDate d = viewModel.date.getValue();
        if (d == null) d = LocalDate.now();
        new DatePickerDialog(requireContext(), (v, y, m, day) -> viewModel.date.setValue(LocalDate.of(y, m + 1, day)), d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth()).show();
    }

    static class TypeAdapter extends RecyclerView.Adapter<TypeAdapter.Holder> {
        List<TxType> list; Context ctx; OnTypeClick listener;
        interface OnTypeClick { void onClick(TxType t); }
        TypeAdapter(Context c, List<TxType> l, OnTypeClick li) { ctx = c; list = l; listener = li; }
        @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new Holder(LayoutInflater.from(ctx).inflate(R.layout.item_type, p, false));
        }
        @Override public void onBindViewHolder(@NonNull Holder h, int p) {
            int pos = h.getAdapterPosition();
            if(pos == RecyclerView.NO_POSITION) return;

            TxType t = list.get(pos);
            String name = "Chi tiêu"; int iconRes = R.drawable.ic_expense; int bgRes = R.drawable.bg_icon_round_accent_1;
            if(t == TxType.INCOME) { name="Thu nhập"; iconRes = R.drawable.ic_income; bgRes = R.drawable.bg_icon_round_success_1; }
            else if (t == TxType.BORROW) { name = "Đi vay"; iconRes = R.drawable.ic_loan; }
            else if (t == TxType.LEND) { name = "Cho vay"; iconRes = R.drawable.ic_giving; }
            h.tv.setText(name); h.icon.setImageResource(iconRes); h.iconBg.setBackgroundResource(bgRes);
            h.itemView.setOnClickListener(v -> listener.onClick(t));
        }
        @Override public int getItemCount() { return list.size(); }
        static class Holder extends RecyclerView.ViewHolder {
            TextView tv; ImageView icon; FrameLayout iconBg;
            Holder(View v) { super(v); tv = v.findViewById(R.id.tvName); icon = v.findViewById(R.id.icon); iconBg = v.findViewById(R.id.iconBg); }
        }
    }

    static class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {
        List<String> icons;
        OnIconSelect listener;
        int selectedPos = 0;

        interface OnIconSelect { void onSelect(String iconName); }

        public IconAdapter(List<String> icons, OnIconSelect listener) {
            this.icons = icons;
            this.listener = listener;
        }

        @NonNull @Override
        public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new IconViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_icon_select, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
            int safePosition = holder.getAdapterPosition();
            if(safePosition == RecyclerView.NO_POSITION) return;

            String iconName = icons.get(safePosition);
            int resId = holder.itemView.getContext().getResources().getIdentifier(iconName, "drawable", holder.itemView.getContext().getPackageName());
            holder.img.setImageResource(resId);

            if (selectedPos == safePosition) {
                holder.bg.setBackgroundResource(R.drawable.bg_icon_round_primary_1);
                holder.img.setColorFilter(Color.WHITE);
            } else {
                holder.bg.setBackgroundResource(R.drawable.bg_icon_round_white);
                holder.img.setColorFilter(Color.BLACK);
            }

            holder.itemView.setOnClickListener(v -> {
                int old = selectedPos;
                selectedPos = safePosition;
                notifyItemChanged(old);
                notifyItemChanged(selectedPos);
                listener.onSelect(iconName);
            });
        }

        @Override public int getItemCount() { return icons.size(); }

        static class IconViewHolder extends RecyclerView.ViewHolder {
            ImageView img; FrameLayout bg;
            public IconViewHolder(View v) { super(v); img = v.findViewById(R.id.imgIcon); bg = v.findViewById(R.id.bgIcon); }
        }
    }
}