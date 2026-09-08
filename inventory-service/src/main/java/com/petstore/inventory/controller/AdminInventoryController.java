package com.petstore.inventory.controller;

import com.petstore.inventory.dto.ApiResponse;
import com.petstore.inventory.dto.InventoryItemDto;
import com.petstore.inventory.dto.UpdateStockRequest;
import com.petstore.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/inventory")
@Tag(name = "Admin Inventory", description = "Endpoints for administrator stock and reservation monitoring")
public class AdminInventoryController {

    private final InventoryService inventoryService;

    public AdminInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping({"", "/items"})
    @Operation(summary = "List all inventory levels (available, reserved, total)")
    public ResponseEntity<ApiResponse<Page<InventoryItemDto>>> getAllInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Page<InventoryItemDto> items = inventoryService.getAllInventory(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok("Inventory items retrieved", items));
    }

    @GetMapping({"/items/{productId:[0-9]+}", "/{productId:[0-9]+}"})
    @Operation(summary = "Get inventory details for a specific product")
    public ResponseEntity<ApiResponse<InventoryItemDto>> getInventory(@PathVariable Long productId) {
        InventoryItemDto item = inventoryService.getInventory(productId);
        return ResponseEntity.ok(ApiResponse.ok("Inventory retrieved", item));
    }

    @PutMapping({"/items/{productId:[0-9]+}/stock", "/{productId:[0-9]+}/stock"})
    @Operation(summary = "Adjust available stock quantity for a product")
    public ResponseEntity<ApiResponse<InventoryItemDto>> updateStock(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateStockRequest request) {
        InventoryItemDto updated = inventoryService.updateStock(productId, request.getStock());
        return ResponseEntity.ok(ApiResponse.ok("Stock updated successfully", updated));
    }
}
