package com.petstore.catalog.event;

import java.math.BigDecimal;

public class ProductUpdatedEvent {

    private Long productId;
    private String name;
    private String slug;
    private String brand;
    private BigDecimal price;
    private String status;
    private Long categoryId;

    public ProductUpdatedEvent() {
    }

    public ProductUpdatedEvent(Long productId, String name, String slug, String brand,
                               BigDecimal price, String status, Long categoryId) {
        this.productId = productId;
        this.name = name;
        this.slug = slug;
        this.brand = brand;
        this.price = price;
        this.status = status;
        this.categoryId = categoryId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
