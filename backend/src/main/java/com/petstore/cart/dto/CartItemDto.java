package com.petstore.cart.dto;

import com.petstore.cart.entity.CartItem;
import com.petstore.product.entity.Product;
import com.petstore.product.entity.ProductImage;

import java.math.BigDecimal;
import java.util.Comparator;

public class CartItemDto {

    private Long id;
    private Long productId;
    private String productName;
    private String productSlug;
    private String productBrand;
    private String productImage;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subtotal;
    private int availableStock;
    private boolean inStock;

    public CartItemDto() {
    }

    public static CartItemDto fromEntity(CartItem item) {
        CartItemDto dto = new CartItemDto();
        dto.setId(item.getId());

        Product product = item.getProduct();
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setProductSlug(product.getSlug());
        dto.setProductBrand(product.getBrand());
        dto.setUnitPrice(product.getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        dto.setAvailableStock(product.getStockQuantity());
        dto.setInStock(product.getStockQuantity() >= item.getQuantity());

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            String primaryUrl = product.getImages().stream()
                    .filter(ProductImage::isPrimary)
                    .map(ProductImage::getImageUrl)
                    .findFirst()
                    .orElse(product.getImages().stream()
                            .min(Comparator.comparingInt(ProductImage::getDisplayOrder))
                            .map(ProductImage::getImageUrl)
                            .orElse(null));
            dto.setProductImage(primaryUrl);
        }

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSlug() {
        return productSlug;
    }

    public void setProductSlug(String productSlug) {
        this.productSlug = productSlug;
    }

    public String getProductBrand() {
        return productBrand;
    }

    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }
}
