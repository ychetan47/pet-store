package com.petstore.order.controller;

import com.petstore.auth.security.UserPrincipal;
import com.petstore.common.response.ApiResponse;
import com.petstore.order.dto.CreateOrderRequest;
import com.petstore.order.dto.OrderResponse;
import com.petstore.order.dto.OrderSummaryDto;
import com.petstore.order.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Orders", description = "Cash on Delivery (COD) checkout, order history, and order cancellation")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", order));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderSummaryDto>>> getOrders(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<OrderSummaryDto> orders = orderService.getCustomerOrders(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {
        OrderResponse order = orderService.getOrderById(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {
        OrderResponse cancelled = orderService.cancelOrder(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", cancelled));
    }
}
