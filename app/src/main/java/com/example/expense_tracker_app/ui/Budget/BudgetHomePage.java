package com.example.expense_tracker_app.ui.Budget;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker_app.R;
import com.example.expense_tracker_app.data.model.Budget;
import com.example.expense_tracker_app.data.repository.BudgetRepository;
import com.example.expense_tracker_app.ui.Budget.BudgetAdapter;
import com.example.expense_tracker_app.data.repository.UserRepository;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BudgetHomePage extends Fragment {

    private BudgetRepository budgetRepository;
    private BudgetAdapter adapter;
    private UserRepository userRepository;
    private int userId = 1;

    private final List<Budget> budgets = new ArrayList<>();
    private final List<Integer> availableYears = new ArrayList<>();
    private final List<Integer> availableMonths = new ArrayList<>();

    private ArrayAdapter<String> yearAdapter;
    private ArrayAdapter<String> monthAdapter;

    private int selectedMonth;
    private int selectedYear;
    private int currentMonth;
    private int currentYear;

    private Spinner spinnerMonth;
    private Spinner spinnerYear;
    private TextView tvSelectedMonthLabel;
    private MaterialButton btnGoCurrentMonth;
    private MaterialButton btnAddBudget;

    private boolean suppressSpinnerCallbacks = false;
    private boolean viewInitialized = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.budget_homepage, container, false);

        spinnerMonth = root.findViewById(R.id.spinnerMonth);
        spinnerYear = root.findViewById(R.id.spinnerYear);
        tvSelectedMonthLabel = root.findViewById(R.id.tvSelectedMonthLabel);
        btnGoCurrentMonth = root.findViewById(R.id.btnGoCurrentMonth);
        btnAddBudget = root.findViewById(R.id.btnAddBudget);
        RecyclerView rvBudgets = root.findViewById(R.id.rvBudgets);

        budgetRepository = new BudgetRepository(requireContext());
        userRepository = new UserRepository(requireContext());
        try {
            com.example.expense_tracker_app.data.model.User u = userRepository.getLoggedInUser();
            if (u != null) userId = u.id;
        } catch (Exception e) { userId = 1; }

        Calendar now = Calendar.getInstance();
        currentMonth = now.get(Calendar.MONTH) + 1;
        currentYear = now.get(Calendar.YEAR);
        selectedMonth = currentMonth;
        selectedYear = currentYear;

        adapter = new BudgetAdapter(budgets, userId);
        rvBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBudgets.setAdapter(adapter);

        // Sự kiện click vào item ngân sách để mở BudgetDetail và truyền subcategoryIds
        adapter.setOnItemClickListener(budget -> {
            Intent intent = new Intent(requireContext(), BudgetDetail.class);
            intent.putExtra("budget_id", budget.getId());
            intent.putExtra("budget_name", budget.getName());
            intent.putExtra("budget_limit", budget.getAmount());
            intent.putExtra("budget_spent", budget.getSpentAmount());
            intent.putExtra("budget_month", budget.getMonth());
            intent.putExtra("budget_year", budget.getYear());
            if (budget.getSubcategoryIds() != null) {
            intent.putIntegerArrayListExtra("subcategory_ids", new ArrayList<>(budget.getSubcategoryIds()));
            }
            startActivity(intent);
        });

        setupSpinnerListeners();
        btnGoCurrentMonth.setOnClickListener(v -> jumpToCurrentMonth());
        btnAddBudget.setOnClickListener(v ->
            startActivity(new Intent(requireContext(), AddBudget.class))
        );

        refreshFiltersAndBudgets();
        viewInitialized = true;
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewInitialized) {
            refreshFiltersAndBudgets();
        }
    }

    private void setupSpinnerListeners() {
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (suppressSpinnerCallbacks || position < 0 || position >= availableMonths.size()) {
                    return;
                }
                selectedMonth = availableMonths.get(position);
                reloadBudgets();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (suppressSpinnerCallbacks || position < 0 || position >= availableYears.size()) {
                    return;
                }
                selectedYear = availableYears.get(position);
                refreshMonthOptions();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void refreshFiltersAndBudgets() {
        availableYears.clear();
        availableYears.addAll(budgetRepository.getAvailableYears());

        if (availableYears.isEmpty()) {
            showEmptyState();
            return;
        }

        spinnerYear.setEnabled(true);
        spinnerMonth.setEnabled(true);
        btnGoCurrentMonth.setEnabled(true);

        if (!availableYears.contains(selectedYear)) {
            selectedYear = availableYears.get(0);
        }

        List<String> yearLabels = new ArrayList<>();
        for (Integer year : availableYears) {
            yearLabels.add(String.valueOf(year));
        }
        yearAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, yearLabels);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        suppressSpinnerCallbacks = true;
        spinnerYear.setAdapter(yearAdapter);
        spinnerYear.setSelection(availableYears.indexOf(selectedYear));
        suppressSpinnerCallbacks = false;

        refreshMonthOptions();
    }

    private void refreshMonthOptions() {
        availableMonths.clear();
        availableMonths.addAll(budgetRepository.getAvailableMonthsForYear(selectedYear));

        if (availableMonths.isEmpty()) {
            showEmptyState();
            return;
        }

        if (!availableMonths.contains(selectedMonth)) {
            selectedMonth = availableMonths.get(availableMonths.size() - 1);
        }

        List<String> monthLabels = new ArrayList<>();
        for (Integer month : availableMonths) {
            monthLabels.add(getMonthLabel(month));
        }
        monthAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, monthLabels);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        suppressSpinnerCallbacks = true;
        spinnerMonth.setAdapter(monthAdapter);
        spinnerMonth.setSelection(availableMonths.indexOf(selectedMonth));
        suppressSpinnerCallbacks = false;

        reloadBudgets();
    }

    private void jumpToCurrentMonth() {
        if (availableYears.isEmpty()) return;
        selectedYear = availableYears.contains(currentYear) ? currentYear : availableYears.get(0);
        selectedMonth = currentMonth;
        suppressSpinnerCallbacks = true;
        spinnerYear.setSelection(availableYears.indexOf(selectedYear));
        suppressSpinnerCallbacks = false;
        refreshMonthOptions();
    }

    private void reloadBudgets() {
        if (availableMonths.isEmpty()) {
            showEmptyState();
            return;
        }
        budgets.clear();
        budgets.addAll(budgetRepository.getBudgetsByMonthYear(selectedMonth, selectedYear));
        adapter.notifyDataSetChanged();
        updateSelectedPeriodLabel();
    }

    private void showEmptyState() {
        budgets.clear();
        adapter.notifyDataSetChanged();
        spinnerYear.setAdapter(null);
        spinnerMonth.setAdapter(null);
        spinnerYear.setEnabled(false);
        spinnerMonth.setEnabled(false);
        btnGoCurrentMonth.setEnabled(false);
        tvSelectedMonthLabel.setText(getString(R.string.label_budget_empty));
    }

    private void updateSelectedPeriodLabel() {
        if (tvSelectedMonthLabel == null || getContext() == null) return;
        tvSelectedMonthLabel.setText(getString(R.string.label_budget_month,
                getMonthLabel(selectedMonth), selectedYear));
    }

    private String getMonthLabel(int month) {
        String[] monthNames = getResources().getStringArray(R.array.months);
        int index = Math.max(0, Math.min(monthNames.length - 1, month - 1));
        return monthNames[index];
    }
}
