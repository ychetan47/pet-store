package com.petstore.category.service;

import com.petstore.category.dto.CategoryResponse;
import com.petstore.category.dto.CategoryTreeResponse;
import com.petstore.category.entity.Category;
import com.petstore.category.repository.CategoryRepository;
import com.petstore.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByIsActiveTrue().stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findByParentCategoryIsNullAndIsActiveTrue();
        return rootCategories.stream()
                .map(CategoryTreeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return CategoryResponse.fromEntity(category);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlugAndIsActiveTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        return CategoryResponse.fromEntity(category);
    }

    @Transactional(readOnly = true)
    public List<Long> getDescendantCategoryIds(Long categoryId) {
        try {
            List<Long> ids = categoryRepository.findDescendantCategoryIds(categoryId);
            if (ids != null && !ids.isEmpty()) {
                return ids;
            }
        } catch (Exception e) {
            // Fallback for in-memory / H2 compatibility where native recursive CTE might differ
            Category root = categoryRepository.findById(categoryId).orElse(null);
            if (root != null) {
                List<Long> fallbackIds = new ArrayList<>();
                collectSubcategoryIds(root, fallbackIds);
                return fallbackIds;
            }
        }
        return Collections.singletonList(categoryId);
    }

    private void collectSubcategoryIds(Category current, List<Long> result) {
        if (!current.isActive()) return;
        result.add(current.getId());
        if (current.getSubcategories() != null) {
            for (Category child : current.getSubcategories()) {
                collectSubcategoryIds(child, result);
            }
        }
    }
}
