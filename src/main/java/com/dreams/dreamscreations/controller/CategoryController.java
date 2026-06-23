package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.Category;
import com.dreams.dreamscreations.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")

public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public Category createCategory(@RequestBody Category category){
        return categoryService.saveCategory(category);
    }

    @GetMapping
    public List<Category> getAllDesigns(){
        return categoryService.getAllCategories();
    }
}
