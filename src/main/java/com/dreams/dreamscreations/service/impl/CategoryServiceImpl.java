package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.Category;
import com.dreams.dreamscreations.repository.CategoryRepository;
import com.dreams.dreamscreations.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository){
        this.categoryRepository =  categoryRepository;
    }

    @Override
    public Category saveCategory(Category category){
        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }

}
