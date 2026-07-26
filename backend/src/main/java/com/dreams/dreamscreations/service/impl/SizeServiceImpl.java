package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.entity.Category;
import com.dreams.dreamscreations.entity.Size;
import com.dreams.dreamscreations.repository.CategoryRepository;
import com.dreams.dreamscreations.repository.SizeRepository;
import com.dreams.dreamscreations.service.SizeService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SizeServiceImpl implements SizeService {

    private final SizeRepository sizeRepository;
    private final CategoryRepository categoryRepository;

    public SizeServiceImpl(SizeRepository sizeRepository,
                           CategoryRepository categoryRepository) {
        this.sizeRepository = sizeRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Size saveSize(Size size) {
        if (size.getCategory() != null && size.getCategory().getCategoryId() != null) {
            Category category = categoryRepository.findById(size.getCategory().getCategoryId())
                    .orElseThrow(() -> new RuntimeException(
                            "Category not found: " + size.getCategory().getCategoryId()));
            size.setCategory(category);
        }
        return sizeRepository.save(size);
    }

    @Override
    public List<Size> getAllSizes() {
        return sizeRepository.findAll();
    }

    @Override
    public Size getSizeById(Long id) {
        return sizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Size not found: " + id));
    }

    @Override
    public List<Size> getSizesByCategoryId(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
        // Filter in memory since SizeRepository has no custom query yet
        return sizeRepository.findAll().stream()
                .filter(s -> s.getCategory().getCategoryId().equals(categoryId))
                .collect(Collectors.toList());
    }
}
