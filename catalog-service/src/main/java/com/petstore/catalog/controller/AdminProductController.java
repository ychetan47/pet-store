package com.petstore.catalog.controller;

import com.petstore.catalog.dto.*;
import com.petstore.catalog.entity.Product;
import com.petstore.catalog.entity.ProductStatus;
import com.petstore.catalog.service.InventoryService;
import com.petstore.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@Tag(name = "Admin Products", description = "Administrator endpoints for product catalog and inventory management")
public class AdminProductController {

    private final ProductService productService;
    private final InventoryService inventoryService;

    public AdminProductController(ProductService productService, InventoryService inventoryService) {
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @Operation(summary = "List products (Admin)", description = "Retrieves all products including inactive items with filters")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAdminProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<ProductResponse> response = productService.getAdminProducts(
                categoryId, brand, status, search, page, size);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", response));
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Creates a new product with optional initial image URL")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {

        ProductDetailResponse created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Updates details of an existing product")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {

        ProductDetailResponse updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Deactivates a product from active catalog")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }

    @PutMapping("/{id}/stock")
    @Operation(summary = "Update stock quantity", description = "Directly sets product inventory level")
    public ResponseEntity<ApiResponse<ProductResponse>> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {

        Product updated = inventoryService.updateStock(id, request.getStock());
        return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", ProductResponse.fromEntity(updated)));
    }

    @PostMapping("/{id}/images")
    @Operation(summary = "Add image URL to product", description = "Adds a hosted image URL with display order and primary flag")
    public ResponseEntity<ApiResponse<ProductImageDto>> addImage(
            @PathVariable Long id,
            @RequestParam String imageUrl,
            @RequestParam(defaultValue = "false") boolean isPrimary,
            @RequestParam(defaultValue = "0") int displayOrder) {

        ProductImageDto imageDto = productService.addImage(id, imageUrl, isPrimary, displayOrder);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Image added successfully", imageDto));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @Operation(summary = "Delete product image", description = "Removes an image from a product")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable Long id,
            @PathVariable Long imageId) {

        productService.deleteImage(id, imageId);
        return ResponseEntity.ok(ApiResponse.success("Image deleted successfully", null));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Admin product stats", description = "Summary statistics including total and low stock items")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats(
            @RequestParam(defaultValue = "10") int lowStockThreshold) {

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", productService.countTotalProducts());
        stats.put("lowStockCount", productService.countLowStockProducts(lowStockThreshold));
        stats.put("lowStockProducts", productService.getLowStockProducts(lowStockThreshold));

        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics", stats));
    }
}
