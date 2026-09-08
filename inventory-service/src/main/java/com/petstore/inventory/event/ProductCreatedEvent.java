package com.petstore.inventory.event;

import java.math.BigDecimal;

public class ProductCreatedEvent {

    private Long productId;
    private String name;
    private String slug;
    private String brand;
    private BigDecimal price;
    private Integer initialStock;
    private String status;
    private Long categoryId;

    public ProductCreatedEvent() {
    }

    public ProductCreatedEvent(Long productId, String name, String slug, String brand,
                               BigDecimal price, Integer initialStock, String status, Long categoryId) {
        this.productId = productId;
        this.name = name;
        this.slug = slug;
        this.brand = brand;
        this.price = price;
        this.initialStock = initialStock;
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

    public Integer getInitialStock() {
        return initialStock;
    }

    public void setInitialStock(Integer initialStock) {
        this.initialStock = initialStock;
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
