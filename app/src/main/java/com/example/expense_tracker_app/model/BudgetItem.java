package com.example.expense_tracker_app.model;
public class BudgetItem {
    private String id;
    private String category;
    private double limitAmount;
    private double spentAmount;
    private double recommendedAmount;
    private double estimatedAmount;
    private String period; // daily, weekly, monthly

    public BudgetItem(String id, String category, double limitAmount,
                      double spentAmount, double recommendedAmount,
                      double estimatedAmount, String period) {
        this.id = id;
        this.category = category;
        this.limitAmount = limitAmount;
        this.spentAmount = spentAmount;
        this.recommendedAmount = recommendedAmount;
        this.estimatedAmount = estimatedAmount;
        this.period = period;
    }

    // Getters
    public String getId() { return id; }
    public String getCategory() { return category; }
    public double getLimitAmount() { return limitAmount; }
    public double getSpentAmount() { return spentAmount; }
    public double getRecommendedAmount() { return recommendedAmount; }
    public double getEstimatedAmount() { return estimatedAmount; }
    public String getPeriod() { return period; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setCategory(String category) { this.category = category; }
    public void setLimitAmount(double limitAmount) { this.limitAmount = limitAmount; }
    public void setSpentAmount(double spentAmount) { this.spentAmount = spentAmount; }
    public void setRecommendedAmount(double recommendedAmount) {
        this.recommendedAmount = recommendedAmount;
    }
    public void setEstimatedAmount(double estimatedAmount) {
        this.estimatedAmount = estimatedAmount;
    }
    public void setPeriod(String period) { this.period = period; }

    // Utility methods
    public int getProgressPercentage() {
        if (limitAmount == 0) return 0;
        return (int) ((spentAmount / limitAmount) * 100);
    }

    public double getRemainingAmount() {
        return limitAmount - spentAmount;
    }

    public boolean isOverBudget() {
        return spentAmount > limitAmount;
    }
}