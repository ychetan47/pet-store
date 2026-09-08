package com.petstore.order.controller;

import com.petstore.order.context.UserContext;
import com.petstore.order.dto.ApiResponse;
import com.petstore.order.dto.CreateOrderRequest;
import com.petstore.order.dto.OrderResponse;
import com.petstore.order.exception.BadRequestException;
import com.petstore.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Cash on Delivery (COD) checkout, order history, and cancellation")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private Long getRequiredUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BadRequestException("Authenticated user context is required to access orders");
        }
        return userId;
    }

    @PostMapping
    @Operation(summary = "Place order", description = "Executes COD checkout with stock reservation and address snapshotting")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Long userId = getRequiredUserId();
        OrderResponse order = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", order));
    }

    @GetMapping
    @Operation(summary = "Get user order history", description = "Lists all past orders placed by the current user")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrders() {
        Long userId = getRequiredUserId();
        List<OrderResponse> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved", orders));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details", description = "Retrieves order tracking, line items, and delivery snapshot")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        Long userId = getRequiredUserId();
        OrderResponse order = orderService.getOrderById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Order details retrieved", order));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel eligible order", description = "Cancels order in PLACED or CONFIRMED status and restores inventory")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long id) {
        Long userId = getRequiredUserId();
        OrderResponse order = orderService.cancelOrder(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }
}
