package com.petstore.order.controller;

import com.petstore.order.dto.ApiResponse;
import com.petstore.order.dto.OrderResponse;
import com.petstore.order.dto.OrderStatusUpdateRequest;
import com.petstore.order.dto.PageResponse;
import com.petstore.order.entity.OrderStatus;
import com.petstore.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "Admin Orders", description = "Administrator endpoints for order processing and status fulfillment")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "List all orders (Admin)", description = "Retrieves all customer orders with status filter and pagination")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAdminOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        PageResponse<OrderResponse> response = orderService.getAdminOrders(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details (Admin)", description = "Retrieves full order information including customer ID and address")
    public ResponseEntity<ApiResponse<OrderResponse>> getAdminOrderById(@PathVariable Long id) {
        OrderResponse order = orderService.getAdminOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved", order));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Transitions order status with workflow validation and inventory restoration if cancelled")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {

        OrderResponse updated = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", updated));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Admin order stats", description = "Order metrics and total revenue summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = orderService.getAdminDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics", stats));
    }
}
