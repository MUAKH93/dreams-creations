package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.Category;
import com.dreams.dreamscreations.entity.Design;

import java.util.List;

public interface CategoryService {
    Category saveCategory(Category category);

    List<Category> getAllCategories();
}

