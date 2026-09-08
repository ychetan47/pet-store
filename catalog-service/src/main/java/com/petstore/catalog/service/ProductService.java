package com.petstore.catalog.service;

import com.petstore.catalog.dto.*;
import com.petstore.catalog.entity.Category;
import com.petstore.catalog.entity.Product;
import com.petstore.catalog.entity.ProductImage;
import com.petstore.catalog.entity.ProductStatus;
import com.petstore.catalog.event.ProductCreatedEvent;
import com.petstore.catalog.event.ProductDeactivatedEvent;
import com.petstore.catalog.event.ProductUpdatedEvent;
import com.petstore.catalog.exception.DuplicateResourceException;
import com.petstore.catalog.exception.ResourceNotFoundException;
import com.petstore.catalog.outbox.OutboxService;
import com.petstore.catalog.repository.CategoryRepository;
import com.petstore.catalog.repository.ProductImageRepository;
import com.petstore.catalog.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryService categoryService;
    private final OutboxService outboxService;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ProductImageRepository productImageRepository,
                          CategoryService categoryService,
                          OutboxService outboxService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.categoryService = categoryService;
        this.outboxService = outboxService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(
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

        Sort sort = Sort.by("id").descending();
        if (sortBy != null && !sortBy.isBlank()) {
            Sort.Direction dir = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = switch (sortBy.toLowerCase()) {
                case "price" -> Sort.by(dir, "price");
                case "name" -> Sort.by(dir, "name");
                case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
                default -> Sort.by(dir, "id");
            };
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sort);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only active products for customers
            predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));

            // Pet filtering (dogs vs cats root tree)
            if (pet != null && !pet.isBlank()) {
                String petSlug = pet.trim().toLowerCase();
                categoryRepository.findBySlugAndIsActiveTrue(petSlug).ifPresent(petCategory -> {
                    List<Long> descendantIds = categoryService.getDescendantCategoryIds(petCategory.getId());
                    if (!descendantIds.isEmpty()) {
                        predicates.add(root.get("category").get("id").in(descendantIds));
                    }
                });
            }

            // Category filtering (including subcategory descendants)
            if (categoryId != null) {
                List<Long> descendantIds = categoryService.getDescendantCategoryIds(categoryId);
                if (!descendantIds.isEmpty()) {
                    predicates.add(root.get("category").get("id").in(descendantIds));
                }
            }

            // Brand filtering
            if (brand != null && !brand.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("brand")), brand.trim().toLowerCase()));
            }

            // Price range filtering
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // Full text search
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), term);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), term);
                Predicate brandMatch = cb.like(cb.lower(root.get("brand")), term);
                predicates.add(cb.or(nameMatch, descMatch, brandMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        Page<ProductResponse> dtoPage = productPage.map(ProductResponse::fromEntity);
        return PageResponse.fromPage(dtoPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAdminProducts(
            Long categoryId,
            String brand,
            ProductStatus status,
            String search,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by("id").descending());

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (brand != null && !brand.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("brand")), brand.trim().toLowerCase()));
            }

            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), term),
                        cb.like(cb.lower(root.get("brand")), term)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return PageResponse.fromPage(productPage.map(ProductResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return ProductDetailResponse.fromEntity(product);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        return ProductDetailResponse.fromEntity(product);
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctBrands() {
        return productRepository.findDistinctBrands();
    }

    @Transactional
    public ProductDetailResponse createProduct(ProductCreateRequest request) {
        String slug = slugify(request.getName());
        if (productRepository.existsBySlug(slug)) {
            slug = slug + "-" + (System.currentTimeMillis() % 10000);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Product product = new Product(
                request.getName().trim(),
                slug,
                request.getDescription(),
                request.getBrand().trim(),
                request.getPrice(),
                request.getStockQuantity(),
                category,
                request.getWeight(),
                request.getStatus() != null ? request.getStatus() : ProductStatus.ACTIVE
        );

        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            ProductImage image = new ProductImage(product, request.getImageUrl().trim(), true, 0);
            product.addImage(image);
        }

        Product saved = productRepository.save(product);
        logger.info("Created product {} ({}) with stock {}", saved.getId(), saved.getName(), saved.getStockQuantity());

        String correlationId = MDC.get("correlationId");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        outboxService.recordEvent(
                "ProductCreated",
                saved.getId().toString(),
                "PRODUCT",
                1,
                correlationId,
                new ProductCreatedEvent(
                        saved.getId(),
                        saved.getName(),
                        saved.getSlug(),
                        saved.getBrand(),
                        saved.getPrice(),
                        saved.getStockQuantity(),
                        saved.getStatus().name(),
                        saved.getCategory().getId()
                )
        );

        return ProductDetailResponse.fromEntity(saved);
    }

    @Transactional
    public ProductDetailResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand().trim());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);
        product.setWeight(request.getWeight());
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        Product updated = productRepository.save(product);
        logger.info("Updated product {} ({})", updated.getId(), updated.getName());

        String correlationId = MDC.get("correlationId");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        outboxService.recordEvent(
                "ProductUpdated",
                updated.getId().toString(),
                "PRODUCT",
                1,
                correlationId,
                new ProductUpdatedEvent(
                        updated.getId(),
                        updated.getName(),
                        updated.getSlug(),
                        updated.getBrand(),
                        updated.getPrice(),
                        updated.getStatus().name(),
                        updated.getCategory().getId()
                )
        );

        return ProductDetailResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Soft delete for safety
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
        logger.info("Deactivated product {}", id);

        String correlationId = MDC.get("correlationId");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        outboxService.recordEvent(
                "ProductDeactivated",
                id.toString(),
                "PRODUCT",
                1,
                correlationId,
                new ProductDeactivatedEvent(id)
        );
    }

    @Transactional
    public ProductImageDto addImage(Long productId, String imageUrl, boolean isPrimary, int displayOrder) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (isPrimary && product.getImages() != null) {
            product.getImages().forEach(img -> img.setPrimary(false));
        }

        ProductImage image = new ProductImage(product, imageUrl.trim(), isPrimary, displayOrder);
        ProductImage saved = productImageRepository.save(image);
        product.getImages().add(saved);
        logger.info("Added image {} to product {}", saved.getId(), productId);
        return ProductImageDto.fromEntity(saved);
    }

    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "id", imageId));

        if (!image.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Image does not belong to product id " + productId);
        }

        productImageRepository.delete(image);
        logger.info("Deleted image {} from product {}", imageId, productId);
    }

    @Transactional(readOnly = true)
    public long countTotalProducts() {
        return productRepository.count();
    }

    @Transactional(readOnly = true)
    public long countLowStockProducts(int threshold) {
        return productRepository.countLowStockProducts(threshold);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold).stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private String slugify(String input) {
        if (input == null) return "";
        String nowhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        return normalized.replaceAll("[^\\w-]", "").toLowerCase(Locale.ENGLISH);
    }
}
