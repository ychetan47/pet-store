package com.petstore.catalog.controller;

import com.petstore.catalog.dto.*;
import com.petstore.catalog.entity.Product;
import com.petstore.catalog.service.InventoryService;
import com.petstore.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/products")
@Hidden // Internal service-to-service communication only
public class InternalProductController {

    private final ProductService productService;
    private final InventoryService inventoryService;

    public InternalProductController(ProductService productService, InventoryService inventoryService) {
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductForOrder(@PathVariable Long id) {
        ProductDetailResponse detail = productService.getProductById(id);
        ProductResponse response = new ProductResponse();
        response.setId(detail.getId());
        response.setName(detail.getName());
        response.setSlug(detail.getSlug());
        response.setBrand(detail.getBrand());
        response.setPrice(detail.getPrice());
        response.setStockQuantity(detail.getStockQuantity());
        response.setCategoryId(detail.getCategoryId());
        response.setCategoryName(detail.getCategoryName());
        response.setWeight(detail.getWeight());
        response.setStatus(detail.getStatus());
        if (!detail.getImages().isEmpty()) {
            response.setPrimaryImageUrl(detail.getImages().get(0).getImageUrl());
        }
        return ResponseEntity.ok(ApiResponse.success("Product fetched", response));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsBatch(@RequestBody List<Long> productIds) {
        List<ProductResponse> products = productIds.stream()
                .map(id -> {
                    ProductDetailResponse detail = productService.getProductById(id);
                    ProductResponse res = new ProductResponse();
                    res.setId(detail.getId());
                    res.setName(detail.getName());
                    res.setPrice(detail.getPrice());
                    res.setStockQuantity(detail.getStockQuantity());
                    res.setStatus(detail.getStatus());
                    if (!detail.getImages().isEmpty()) {
                        res.setPrimaryImageUrl(detail.getImages().get(0).getImageUrl());
                    }
                    return res;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Batch products fetched", products));
    }

    @PostMapping("/{id}/reserve-stock")
    public ResponseEntity<ApiResponse<StockReservationResponse>> reserveStock(
            @PathVariable Long id,
            @Valid @RequestBody StockReservationRequest request) {

        Product product = inventoryService.lockAndDeductStock(id, request.getQuantity());

        StockReservationResponse response = new StockReservationResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                request.getQuantity(),
                product.getStockQuantity(),
                true
        );

        return ResponseEntity.ok(ApiResponse.success("Stock reserved successfully", response));
    }

    @PostMapping("/{id}/release-stock")
    public ResponseEntity<ApiResponse<Void>> releaseStock(
            @PathVariable Long id,
            @Valid @RequestBody StockReservationRequest request) {

        inventoryService.restoreStock(id, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Stock released successfully", null));
    }
}
