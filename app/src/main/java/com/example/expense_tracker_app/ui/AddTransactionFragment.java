package com.example.expense_tracker_app.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.CategoryWithSubcategories;
import com.example.expense_tracker_app.data.model.Subcategory;
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

    // --- BIẾN QUẢN LÝ DIALOG DANH MỤC ---
    private BottomSheetDialog categoryDialog;
    private LinearLayout categoryContainer;

    // --- BIẾN XỬ LÝ ẢNH ---
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Uri> takePhotoLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private Uri currentPhotoUri;

    // Icon mặc định cho tạo mới
    private final List<String> AVAILABLE_ICONS = Arrays.asList(
            "ic_cat_food", "ic_cat_coffee", "ic_cat_groceries",
            "ic_cat_transport", "ic_cat_home",
            "ic_cat_health", "ic_cat_car_service", "ic_cat_insurance",
            "ic_cat_sport", "ic_cat_music", "ic_cat_travel", "ic_cat_gamepad"
    );
    private String tempSelectedIconName = "ic_cat_food";

    // Biến này không còn dùng cho Group nữa, nhưng giữ lại cho Subcategory
    private String newCategoryIcon = "ic_cat_food";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);
        viewModel = new ViewModelProvider(this).get(AddTxViewModel.class);

        initViews(view);
        registerImageLaunchers();
        setupEvents();
        observeViewModel();
        return view;
    }

    private void registerImageLaunchers() {
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                String internalPath = saveImageToInternalStorage(uri);
                if (internalPath != null) {
                    viewModel.imagePath.setValue(internalPath);
                    Toast.makeText(getContext(), "Đã chọn ảnh!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        takePhotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success && currentPhotoUri != null) {
                viewModel.imagePath.setValue(currentPhotoUri.toString());
                Toast.makeText(getContext(), "Đã chụp ảnh!", Toast.LENGTH_SHORT).show();
            }
        });

        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) dispatchTakePictureIntent();
            else Toast.makeText(getContext(), "Cần quyền Camera!", Toast.LENGTH_SHORT).show();
        });
    }

    private void showImageSourceDialog() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Thêm hình ảnh")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            dispatchTakePictureIntent();
                        } else {
                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                        }
                    } else {
                        pickImageLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private void dispatchTakePictureIntent() {
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(requireContext(),
                        requireContext().getPackageName() + ".fileprovider", photoFile);
                takePhotoLauncher.launch(currentPhotoUri);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi Camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() {
        try {
            return File.createTempFile("IMG_" + System.currentTimeMillis() + "_", ".jpg", requireContext().getFilesDir());
        } catch (Exception e) { return null; }
    }

    private String saveImageToInternalStorage(Uri sourceUri) {
        try {
            File file = new File(requireContext().getFilesDir(), "img_" + System.currentTimeMillis() + ".jpg");
            InputStream in = requireContext().getContentResolver().openInputStream(sourceUri);
            FileOutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024]; int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            out.close(); in.close();
            return Uri.fromFile(file).toString();
        } catch (Exception e) { return null; }
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
                    TextView tv = (TextView) child;
                    if (!tv.getText().toString().equals("Thêm hình ảnh")) {
                        tvImageStatus = tv;
                        break;
                    }
                }
            }
        }

        btnSave.setAlpha(0.5f);
        btnSave.setEnabled(false);
    }

    private void setupEvents() {
        if (toolbar != null) toolbar.setOnClickListener(v -> requireActivity().onBackPressed());

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.amount.setValue(s.toString());
                boolean hasMoney = s.length() > 0;
                btnSave.setAlpha(hasMoney ? 1.0f : 0.5f);
                btnSave.setEnabled(hasMoney);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        rowType.setOnClickListener(v -> showTypePickerDialog());
        rowCategory.setOnClickListener(v -> showCategoryPickerDialog());
        rowDate.setOnClickListener(v -> showDatePicker());

        optCash.setOnClickListener(v -> {
            if (Boolean.TRUE.equals(viewModel.hasCashWallet.getValue())) {
                viewModel.method.setValue("Tiền mặt");
                updateMethodUI(true);
            } else {
                Toast.makeText(getContext(), "Bạn chưa tạo Ví tiền mặt!", Toast.LENGTH_SHORT).show();
            }
        });

        optTransfer.setOnClickListener(v -> {
            if (Boolean.TRUE.equals(viewModel.hasBankWallet.getValue())) {
                viewModel.method.setValue("Chuyển khoản");
                updateMethodUI(false);
            } else {
                Toast.makeText(getContext(), "Bạn chưa tạo Ví chuyển khoản!", Toast.LENGTH_SHORT).show();
            }
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
            input.setText(viewModel.note.getValue());
            new AlertDialog.Builder(getContext()).setTitle("Ghi chú").setView(input)
                    .setPositiveButton("OK", (d, w) -> viewModel.note.setValue(input.getText().toString())).setNegativeButton("Hủy", null).show();
        });

        rowLocation.setOnClickListener(v -> {
            EditText input = new EditText(getContext());
            input.setText(viewModel.location.getValue());
            new AlertDialog.Builder(getContext()).setTitle("Địa điểm").setView(input)
                    .setPositiveButton("OK", (d, w) -> viewModel.location.setValue(input.getText().toString())).setNegativeButton("Hủy", null).show();
        });

        rowImage.setOnClickListener(v -> showImageSourceDialog());

        switchReport.setOnCheckedChangeListener((btn, isChecked) -> {
            viewModel.excludeReport.setValue(isChecked);
            tvReportHint.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if(isChecked) tvReportHint.setText("Giao dịch này sẽ không tính vào báo cáo.");
        });

        btnSave.setOnClickListener(v -> {
            if (etAmount.getText().toString().isEmpty()) Toast.makeText(getContext(), "Nhập số tiền", Toast.LENGTH_SHORT).show();
            else viewModel.submit();
        });
    }

    private void showCategoryPickerDialog() {
        categoryDialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.sheet_pick_category, null);
        categoryDialog.setContentView(view);

        categoryContainer = view.findViewById(R.id.ll_category_container);

        ImageView btnAddGroup = view.findViewById(R.id.btn_add_group);
        if (btnAddGroup != null) {
            // SỬA: Gọi dialog nhập tên đơn giản
            btnAddGroup.setOnClickListener(v -> showAddCategoryGroupDialog(categoryDialog));
        }

        List<CategoryWithSubcategories> data = viewModel.categories.getValue();
        if (data == null || data.isEmpty()) {
            viewModel.refreshCategories();
        } else {
            renderCategoryList(data);
        }

        categoryDialog.show();
    }

    private void renderCategoryList(List<CategoryWithSubcategories> data) {
        if (categoryContainer == null) return;
        categoryContainer.removeAllViews();

        for (CategoryWithSubcategories cws : data) {
            if (cws == null || cws.category == null) continue;

            TextView title = new TextView(requireContext());
            title.setText(cws.category.name);
            title.setTextSize(16);
            title.setTextColor(getResources().getColor(R.color.neutral_900, null));
            title.setPadding(0, 30, 0, 20);
            categoryContainer.addView(title);

            GridLayout grid = new GridLayout(requireContext());
            grid.setColumnCount(4);
            grid.setUseDefaultMargins(true);

            if (cws.subcategories != null) {
                for (Subcategory sub : cws.subcategories) {
                    addGridItem(grid, sub.name, sub.icon, v -> {
                        viewModel.category.setValue(cws.category);
                        viewModel.subcategory.setValue(sub);
                        viewModel.subcategoryId.setValue(sub.id);
                        if(categoryDialog != null) categoryDialog.dismiss();
                    }, false);
                }
            }

            addGridItem(grid, "Thêm", "ic_add", v -> showAddSubcategoryDialog(cws, categoryDialog), true);
            categoryContainer.addView(grid);
        }
    }

    private void addGridItem(GridLayout grid, String name, String iconResName, View.OnClickListener onClickListener, boolean isAddItem) {
        LinearLayout item = new LinearLayout(requireContext());
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        item.setLayoutParams(params);

        item.setPadding(10, 10, 10, 10);
        item.setBackgroundResource(R.drawable.bg_chip_category_state);

        FrameLayout iconBg = new FrameLayout(requireContext());
        int bgSize = (int) (48 * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams bgParams = new FrameLayout.LayoutParams(bgSize, bgSize);
        iconBg.setLayoutParams(bgParams);
        iconBg.setBackgroundResource(isAddItem ? R.drawable.bg_icon_round_primary_2 : R.drawable.bg_icon_round_accent_1);

        ImageView iv = new ImageView(requireContext());
        int resId = 0;
        if (iconResName != null && !iconResName.isEmpty()) {
            if ("ic_add".equals(iconResName)) resId = android.R.drawable.ic_input_add;
            else resId = getResources().getIdentifier(iconResName, "drawable", requireContext().getPackageName());
        }
        if (resId == 0) resId = R.drawable.ic_category;

        iv.setImageResource(resId);
        iv.setColorFilter(Color.WHITE);
        int iconSize = (int) (24 * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams iconLp = new FrameLayout.LayoutParams(iconSize, iconSize);
        iconLp.gravity = Gravity.CENTER;
        iv.setLayoutParams(iconLp);
        iconBg.addView(iv);

        TextView tv = new TextView(requireContext());
        tv.setText(name);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(12);
        tv.setMaxLines(2);
        tv.setPadding(0, 10, 0, 0);
        tv.setTextColor(getResources().getColor(R.color.neutral_900, null));

        item.addView(iconBg);
        item.addView(tv);
        item.setOnClickListener(onClickListener);

        grid.addView(item);
    }

    private void showAddSubcategoryDialog(CategoryWithSubcategories cws, BottomSheetDialog parentSheet) {
        if (cws == null || cws.category == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if(dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etName = view.findViewById(R.id.etCatName);
        Button btnConfirm = view.findViewById(R.id.btnConfirmAdd);
        RecyclerView rvIcons = view.findViewById(R.id.rvIcons);

        rvIcons.setLayoutManager(new GridLayoutManager(getContext(), 5));
        IconAdapter iconAdapter = new IconAdapter(AVAILABLE_ICONS, iconName -> tempSelectedIconName = iconName);
        rvIcons.setAdapter(iconAdapter);

        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if(!name.isEmpty()) {
                viewModel.addNewSubcategory(cws.category.id, name, tempSelectedIconName);
                Toast.makeText(getContext(), "Đã thêm vào " + cws.category.name, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    // --- SỬA: Hàm này giờ chỉ hiện 1 ô nhập text ---
    private void showAddCategoryGroupDialog(BottomSheetDialog parentSheet) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Tạo nhóm danh mục mới");

        // Tạo layout động chứa ô input
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 20);

        EditText input = new EditText(requireContext());
        input.setHint("Nhập tên nhóm (VD: Đầu tư)");
        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton("Tạo", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                // Gọi hàm tạo nhóm mới trong ViewModel
                viewModel.addNewGroup(name);
                Toast.makeText(getContext(), "Đã tạo nhóm: " + name, Toast.LENGTH_SHORT).show();
                // Không cần dismiss parentSheet, Observer sẽ tự update lại danh sách
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    // ------------------------------------------------

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

    private void observeViewModel() {
        viewModel.date.observe(getViewLifecycleOwner(), d -> tvDate.setText(d.getDayOfMonth() + "/" + d.getMonthValue() + "/" + d.getYear()));
        viewModel.type.observe(getViewLifecycleOwner(), t -> {
            String s = "Chi tiêu";
            if (t == TxType.INCOME) s = "Thu nhập";
            else if (t == TxType.BORROW) s = "Đi vay";
            else if (t == TxType.LEND) s = "Cho vay";
            tvType.setText(s);
            tvCategory.setText("Chọn danh mục");
            viewModel.category.setValue(null);
            viewModel.subcategory.setValue(null);
            viewModel.refreshCategories();
        });
        viewModel.subcategory.observe(getViewLifecycleOwner(), sub -> { if (sub != null) tvCategory.setText(sub.name); });
        viewModel.category.observe(getViewLifecycleOwner(), cat -> { if (cat != null && viewModel.subcategory.getValue() == null) tvCategory.setText(cat.name); });
        viewModel.method.observe(getViewLifecycleOwner(), m -> updateMethodUI("Tiền mặt".equals(m)));

        // Tự động cập nhật Dialog khi dữ liệu thay đổi
        viewModel.categories.observe(getViewLifecycleOwner(), data -> {
            if (categoryDialog != null && categoryDialog.isShowing()) {
                renderCategoryList(data);
            }
        });

        viewModel.imagePath.observe(getViewLifecycleOwner(), path -> {
            if (tvImageStatus != null) {
                tvImageStatus.setText((path != null && !path.isEmpty()) ? "Đã có ảnh" : "Thêm hình ảnh");
                tvImageStatus.setTextColor((path != null && !path.isEmpty()) ? getResources().getColor(R.color.primary_1, null) : getResources().getColor(R.color.neutral_700, null));
            }
        });

        viewModel.done.observe(getViewLifecycleOwner(), done -> {
            if (done) {
                Toast.makeText(getContext(), "Lưu thành công", Toast.LENGTH_SHORT).show();
                etAmount.setText("");
                if (switchReport != null) switchReport.setChecked(false);
                viewModel.resetInput();
            }
        });
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
        List<String> icons; OnIconSelect listener; int selectedPos = 0;
        interface OnIconSelect { void onSelect(String iconName); }
        public IconAdapter(List<String> icons, OnIconSelect listener) { this.icons = icons; this.listener = listener; }
        @NonNull @Override public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new IconViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_icon_select, parent, false));
        }
        @Override public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
            String iconName = icons.get(position);
            int resId = holder.itemView.getContext().getResources().getIdentifier(iconName, "drawable", holder.itemView.getContext().getPackageName());
            holder.img.setImageResource(resId);
            if (selectedPos == position) {
                holder.bg.setBackgroundResource(R.drawable.bg_icon_round_primary_1); holder.img.setColorFilter(Color.WHITE);
            } else {
                holder.bg.setBackgroundResource(R.drawable.bg_icon_round_white); holder.img.setColorFilter(Color.BLACK);
            }
            holder.itemView.setOnClickListener(v -> {
                int old = selectedPos; selectedPos = holder.getAdapterPosition();
                notifyItemChanged(old); notifyItemChanged(selectedPos);
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