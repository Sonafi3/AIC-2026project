package com.zlagoda.model;

public class Category {
    private int categoryNumber;
    private String categoryName;

    public Category() {
    }

    public Category(int categoryNumber, String categoryName) {
        this.categoryNumber = categoryNumber;
        this.categoryName = categoryName;
    }

    public int getCategoryNumber() {
        return categoryNumber;
    }

    public void setCategoryNumber(int categoryNumber) {
        this.categoryNumber = categoryNumber;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}