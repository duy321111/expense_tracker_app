package com.example.expense_tracker_app.data.model;

import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;

public class CategoryWithSubcategories {

    @Embedded
    public Category category;

    @Relation(parentColumn = "id", entityColumn = "categoryId")
    public List<Subcategory> subcategories;
}
