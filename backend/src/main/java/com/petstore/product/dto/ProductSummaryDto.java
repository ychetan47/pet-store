package com.petstore.product.dto;

import com.petstore.product.entity.Product;
import com.petstore.product.entity.ProductImage;

import java.math.BigDecimal;
import java.util.Comparator;

public class ProductSummaryDto {

    private Long id;
    private String name;
    private String slug;
    private String brand;
    private BigDecimal price;
    private int stockQuantity;
    private boolean inStock;
    private String primaryImageUrl;
    private Long categoryId;
    private String categoryName;
    private String weight;

    public ProductSummaryDto() {
    }

    public static ProductSummaryDto fromEntity(Product product) {
        ProductSummaryDto dto = new ProductSummaryDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSlug(product.getSlug());
        dto.setBrand(product.getBrand());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setInStock(product.getStockQuantity() > 0);
        dto.setWeight(product.getWeight());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            String primaryUrl = product.getImages().stream()
                    .filter(ProductImage::isPrimary)
                    .map(ProductImage::getImageUrl)
                    .findFirst()
                    .orElse(product.getImages().stream()
                            .min(Comparator.comparingInt(ProductImage::getDisplayOrder))
                            .map(ProductImage::getImageUrl)
                            .orElse(null));
            dto.setPrimaryImageUrl(primaryUrl);
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

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
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

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
}
