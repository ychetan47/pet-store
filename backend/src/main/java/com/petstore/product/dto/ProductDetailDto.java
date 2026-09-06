package com.petstore.product.dto;

import com.petstore.category.entity.Category;
import com.petstore.product.entity.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ProductDetailDto {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String brand;
    private BigDecimal price;
    private int stockQuantity;
    private boolean inStock;
    private String weight;
    private Long categoryId;
    private String categoryName;
    private List<String> categoryBreadcrumbs = new ArrayList<>();
    private List<ProductImageDto> images = new ArrayList<>();

    public ProductDetailDto() {
    }

    public static ProductDetailDto fromEntity(Product product) {
        ProductDetailDto dto = new ProductDetailDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSlug(product.getSlug());
        dto.setDescription(product.getDescription());
        dto.setBrand(product.getBrand());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setInStock(product.getStockQuantity() > 0);
        dto.setWeight(product.getWeight());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());

            // Build breadcrumb chain from root to leaf
            List<String> crumbs = new ArrayList<>();
            Category curr = product.getCategory();
            while (curr != null) {
                crumbs.add(curr.getName());
                curr = curr.getParentCategory();
            }
            Collections.reverse(crumbs);
            dto.setCategoryBreadcrumbs(crumbs);
        }

        if (product.getImages() != null) {
            dto.setImages(product.getImages().stream()
                    .map(ProductImageDto::fromEntity)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<String> getCategoryBreadcrumbs() {
        return categoryBreadcrumbs;
    }

    public void setCategoryBreadcrumbs(List<String> categoryBreadcrumbs) {
        this.categoryBreadcrumbs = categoryBreadcrumbs;
    }

    public List<ProductImageDto> getImages() {
        return images;
    }

    public void setImages(List<ProductImageDto> images) {
        this.images = images;
    }
}
