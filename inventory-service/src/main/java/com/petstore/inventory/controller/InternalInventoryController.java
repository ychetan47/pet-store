package com.petstore.inventory.controller;

import com.petstore.inventory.dto.ApiResponse;
import com.petstore.inventory.dto.InventoryItemDto;
import com.petstore.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/inventory")
@Tag(name = "Internal Inventory", description = "Internal service-to-service synchronous stock inquiry endpoints")
public class InternalInventoryController {

    private final InventoryService inventoryService;

    public InternalInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get current stock for a product (Internal)")
    public ResponseEntity<ApiResponse<InventoryItemDto>> getStock(@PathVariable Long productId) {
        InventoryItemDto item = inventoryService.getInventory(productId);
        return ResponseEntity.ok(ApiResponse.ok("Stock retrieved", item));
    }

    @PostMapping("/batch")
    @Operation(summary = "Batch get current stock for multiple products (Internal)")
    public ResponseEntity<ApiResponse<List<InventoryItemDto>>> getStockBatch(@RequestBody List<Long> productIds) {
        List<InventoryItemDto> items = inventoryService.getInventoryBatch(productIds);
        return ResponseEntity.ok(ApiResponse.ok("Batch stock retrieved", items));
    }
}
