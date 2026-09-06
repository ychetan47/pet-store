package com.petstore.product.service;

import com.petstore.category.dto.CategoryResponse;
import com.petstore.category.service.CategoryService;
import com.petstore.common.exception.ResourceNotFoundException;
import com.petstore.common.response.PageResponse;
import com.petstore.product.dto.ProductDetailDto;
import com.petstore.product.dto.ProductSummaryDto;
import com.petstore.product.entity.Product;
import com.petstore.product.entity.ProductStatus;
import com.petstore.product.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryDto> getProducts(
            String pet,
            Long categoryId,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        Sort sort = Sort.by("asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy != null && !sortBy.isBlank() ? sortBy : "createdAt");
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sort);

        List<Long> targetCategoryIds = new ArrayList<>();

        if (categoryId != null) {
            targetCategoryIds.addAll(categoryService.getDescendantCategoryIds(categoryId));
        } else if (pet != null && !pet.isBlank()) {
            try {
                CategoryResponse petCategory = categoryService.getCategoryBySlug(pet.trim().toLowerCase());
                targetCategoryIds.addAll(categoryService.getDescendantCategoryIds(petCategory.getId()));
            } catch (Exception ignored) {
                // If pet slug doesn't exist, ignore or filter will result empty
            }
        }

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always ACTIVE
            predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));

            // Categories
            if (!targetCategoryIds.isEmpty()) {
                predicates.add(root.get("category").get("id").in(targetCategoryIds));
            }

            // Brand
            if (brand != null && !brand.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("brand")), brand.trim().toLowerCase()));
            }

            // Price Range
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // Search query
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                Predicate brandMatch = cb.like(cb.lower(root.get("brand")), pattern);
                predicates.add(cb.or(nameMatch, descMatch, brandMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        Page<ProductSummaryDto> dtoPage = productPage.map(ProductSummaryDto::fromEntity);
        return PageResponse.from(dtoPage);
    }

    @Transactional(readOnly = true)
    public ProductDetailDto getProductById(Long id) {
        Product product = productRepository.findByIdAndStatus(id, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return ProductDetailDto.fromEntity(product);
    }

    @Transactional(readOnly = true)
    public ProductDetailDto getProductBySlug(String slug) {
        Product product = productRepository.findBySlugAndStatus(slug, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        return ProductDetailDto.fromEntity(product);
    }

    @Transactional(readOnly = true)
    public List<String> getAvailableBrands() {
        return productRepository.findDistinctBrands();
    }
}
