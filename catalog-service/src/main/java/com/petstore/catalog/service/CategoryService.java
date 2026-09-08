package com.petstore.catalog.service;

import com.petstore.catalog.dto.CategoryCreateRequest;
import com.petstore.catalog.dto.CategoryResponse;
import com.petstore.catalog.dto.CategoryTreeResponse;
import com.petstore.catalog.dto.CategoryUpdateRequest;
import com.petstore.catalog.entity.Category;
import com.petstore.catalog.exception.DuplicateResourceException;
import com.petstore.catalog.exception.ResourceNotFoundException;
import com.petstore.catalog.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findByIsActiveTrueOrderByNameAsc();

        Map<Long, List<Category>> childrenMap = new HashMap<>();
        List<Category> rootCategories = new ArrayList<>();

        for (Category cat : allCategories) {
            if (cat.getParent() == null) {
                rootCategories.add(cat);
            } else {
                childrenMap.computeIfAbsent(cat.getParent().getId(), k -> new ArrayList<>()).add(cat);
            }
        }

        return rootCategories.stream()
                .map(root -> buildTreeNode(root, childrenMap))
                .collect(Collectors.toList());
    }

    private CategoryTreeResponse buildTreeNode(Category category, Map<Long, List<Category>> childrenMap) {
        CategoryTreeResponse node = new CategoryTreeResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getImageUrl()
        );

        List<Category> children = childrenMap.getOrDefault(category.getId(), Collections.emptyList());
        for (Category child : children) {
            node.getSubcategories().add(buildTreeNode(child, childrenMap));
        }

        return node;
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return CategoryResponse.fromEntity(category);
    }

    @Transactional(readOnly = true)
    public List<Long> getDescendantCategoryIds(Long categoryId) {
        return categoryRepository.findDescendantCategoryIds(categoryId);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        String slug = request.getSlug() != null && !request.getSlug().isBlank()
                ? slugify(request.getSlug())
                : slugify(request.getName());

        if (categoryRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis() % 10000;
        }

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category", "id", request.getParentId()));
        }

        Category category = new Category(
                request.getName().trim(),
                slug,
                parent,
                request.getImageUrl() != null ? request.getImageUrl().trim() : null,
                request.isActive()
        );

        Category saved = categoryRepository.save(category);
        logger.info("Created category {} ({}) with slug {}", saved.getId(), saved.getName(), saved.getSlug());
        return CategoryResponse.fromEntity(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = slugify(request.getSlug());
            if (!newSlug.equals(category.getSlug()) && categoryRepository.existsBySlug(newSlug)) {
                throw new DuplicateResourceException("Category slug already in use: " + newSlug);
            }
            category.setSlug(newSlug);
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new IllegalArgumentException("A category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category", "id", request.getParentId()));
            category.setParent(parent);
        }

        category.setName(request.getName().trim());
        category.setImageUrl(request.getImageUrl() != null ? request.getImageUrl().trim() : null);
        category.setActive(request.isActive());

        Category updated = categoryRepository.save(category);
        logger.info("Updated category {} ({})", updated.getId(), updated.getName());
        return CategoryResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Soft delete / deactivate
        category.setActive(false);
        categoryRepository.save(category);
        logger.info("Deactivated category {}", id);
    }

    private String slugify(String input) {
        if (input == null) return "";
        String nowhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        return normalized.replaceAll("[^\\w-]", "").toLowerCase(Locale.ENGLISH);
    }
}
