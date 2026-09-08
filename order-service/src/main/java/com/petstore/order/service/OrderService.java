package com.petstore.order.service;

import com.petstore.order.client.ProductCatalogClient;
import com.petstore.order.client.UserServiceClient;
import com.petstore.order.dto.*;
import com.petstore.order.entity.*;
import com.petstore.order.event.*;
import com.petstore.order.exception.BadRequestException;
import com.petstore.order.exception.ResourceNotFoundException;
import com.petstore.order.outbox.OutboxService;
import com.petstore.order.repository.CartRepository;
import com.petstore.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final ProductCatalogClient productCatalogClient;
    private final UserServiceClient userServiceClient;
    private final OutboxService outboxService;
    private final com.petstore.order.metrics.OrderMetrics orderMetrics;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        CartService cartService,
                        ProductCatalogClient productCatalogClient,
                        UserServiceClient userServiceClient,
                        OutboxService outboxService,
                        com.petstore.order.metrics.OrderMetrics orderMetrics) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.productCatalogClient = productCatalogClient;
        this.userServiceClient = userServiceClient;
        this.outboxService = outboxService;
        this.orderMetrics = orderMetrics;
    }

    /**
     * Executes the checkout flow:
     * 1. Validates address snapshot from User Service
     * 2. Retrieves current customer cart
     * 3. Fetches latest product prices & validates active status from Catalog Service
     * 4. Creates Order with status PLACED and address & item snapshots
     * 5. Clears shopping cart
     * 6. Records OrderCreated event in outbox within the same transaction
     */
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        // 1. Fetch address snapshot from User Service
        AddressDto address = userServiceClient.getAddress(request.getAddressId(), userId);
        if (address == null) {
            throw new BadRequestException("Delivery address not found: " + request.getAddressId());
        }

        // 2. Retrieve user's cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("No active cart found for checkout"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot checkout an empty cart");
        }

        // 3. Fetch product details and compute order totals
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            ProductDto product = productCatalogClient.getProduct(item.getProductId());
            if (product == null || !product.isActive()) {
                throw new BadRequestException("Product is unavailable or deactivated: " + item.getProductId());
            }
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);

            orderItems.add(new OrderItem(
                    null,
                    product.getId(),
                    product.getName(),
                    item.getQuantity(),
                    product.getPrice(),
                    lineTotal
            ));
        }

        // 4. Create Order with complete address snapshot
        Order order = new Order(
                userId,
                totalAmount,
                OrderStatus.PLACED,
                request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.COD,
                PaymentStatus.PENDING,
                address.getName(),
                address.getPhone(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPincode()
        );

        for (OrderItem oi : orderItems) {
            order.addItem(oi);
        }

        Order savedOrder = orderRepository.save(order);
        logger.info("Created COD order {} for user {} with total amount {}", savedOrder.getId(), userId, totalAmount);

        // 5. Clear cart
        cartService.clearCart(userId);

        // 6. Record OrderCreated event in outbox within same transaction
        String formattedAddress = String.format("%s, %s, %s, %s - %s",
                address.getAddressLine1(),
                address.getAddressLine2() != null && !address.getAddressLine2().isBlank() ? address.getAddressLine2() : "",
                address.getCity(),
                address.getState(),
                address.getPincode()).replaceAll(", ,", ",");

        List<OrderCreatedEvent.OrderItemPayload> eventItems = savedOrder.getItems().stream()
                .map(oi -> new OrderCreatedEvent.OrderItemPayload(
                        oi.getProductId(),
                        oi.getProductName(),
                        oi.getQuantity(),
                        oi.getPrice(),
                        oi.getTotalPrice()
                ))
                .collect(Collectors.toList());

        String correlationId = MDC.get("correlationId");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        OrderCreatedEvent payload = new OrderCreatedEvent(
                savedOrder.getId(),
                userId,
                savedOrder.getTotalAmount(),
                savedOrder.getShippingName(),
                savedOrder.getShippingPhone(),
                formattedAddress,
                eventItems
        );

        outboxService.recordEvent(
                "OrderCreated",
                savedOrder.getId().toString(),
                "ORDER",
                1,
                correlationId,
                payload
        );

        orderMetrics.incrementOrdersCreated();

        return OrderResponse.fromEntity(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, Long userId) {
        Order order = orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return OrderResponse.fromEntity(order);
    }

    /**
     * Customer order cancellation: transitions order to CANCELLED and publishes OrderCancelled event.
     */
    @Transactional
    public OrderResponse cancelOrder(Long id, Long userId) {
        Order order = orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        if (!order.getOrderStatus().isCancellable()) {
            throw new BadRequestException("Order cannot be cancelled in status: " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.CANCELLED);

        Order updated = orderRepository.save(order);
        logger.info("Order {} cancelled by user {}. Publishing OrderCancelled to outbox.", id, userId);

        String correlationId = MDC.get("correlationId");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        outboxService.recordEvent(
                "OrderCancelled",
                order.getId().toString(),
                "ORDER",
                1,
                correlationId,
                new OrderCancelledEvent(order.getId(), order.getUserId(), "Cancelled by customer")
        );

        orderMetrics.incrementOrdersCancelled();

        return OrderResponse.fromEntity(updated);
    }

    // --- Admin Operations ---

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAdminOrders(OrderStatus status, Pageable pageable) {
        Page<Order> page;
        if (status != null) {
            page = orderRepository.findByOrderStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            page = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return PageResponse.fromPage(page.map(OrderResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public OrderResponse getAdminOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        OrderStatus currentStatus = order.getOrderStatus();
        if (!currentStatus.isValidTransition(newStatus)) {
            throw new BadRequestException(String.format("Invalid order status transition from %s to %s", currentStatus, newStatus));
        }

        String correlationId = MDC.get("correlationId");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        if (newStatus == OrderStatus.CANCELLED && currentStatus != OrderStatus.CANCELLED) {
            order.setPaymentStatus(PaymentStatus.CANCELLED);
            order.setOrderStatus(newStatus);
            Order updated = orderRepository.save(order);

            outboxService.recordEvent(
                    "OrderCancelled",
                    order.getId().toString(),
                    "ORDER",
                    1,
                    correlationId,
                    new OrderCancelledEvent(order.getId(), order.getUserId(), "Cancelled by administrator")
            );
            return OrderResponse.fromEntity(updated);
        } else if (newStatus == OrderStatus.DELIVERED) {
            order.setPaymentStatus(PaymentStatus.COMPLETED); // COD collected on delivery
            order.setOrderStatus(newStatus);
            Order updated = orderRepository.save(order);

            outboxService.recordEvent(
                    "OrderDelivered",
                    order.getId().toString(),
                    "ORDER",
                    1,
                    correlationId,
                    new OrderDeliveredEvent(order.getId(), order.getUserId())
            );
            return OrderResponse.fromEntity(updated);
        } else if (newStatus == OrderStatus.SHIPPED) {
            order.setOrderStatus(newStatus);
            Order updated = orderRepository.save(order);

            outboxService.recordEvent(
                    "OrderShipped",
                    order.getId().toString(),
                    "ORDER",
                    1,
                    correlationId,
                    new OrderShippedEvent(order.getId(), order.getUserId())
            );
            return OrderResponse.fromEntity(updated);
        } else if (newStatus == OrderStatus.CONFIRMED) {
            order.setOrderStatus(newStatus);
            Order updated = orderRepository.save(order);

            outboxService.recordEvent(
                    "OrderConfirmed",
                    order.getId().toString(),
                    "ORDER",
                    1,
                    correlationId,
                    new OrderConfirmedEvent(order.getId(), order.getUserId(), order.getTotalAmount())
            );
            return OrderResponse.fromEntity(updated);
        }

        order.setOrderStatus(newStatus);
        Order updated = orderRepository.save(order);
        logger.info("Admin updated order {} status from {} to {}", id, currentStatus, newStatus);
        return OrderResponse.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalOrders = orderRepository.count();
        long placed = orderRepository.countByOrderStatus(OrderStatus.PLACED);
        long confirmed = orderRepository.countByOrderStatus(OrderStatus.CONFIRMED);
        long shipped = orderRepository.countByOrderStatus(OrderStatus.SHIPPED);
        long delivered = orderRepository.countByOrderStatus(OrderStatus.DELIVERED);
        long cancelled = orderRepository.countByOrderStatus(OrderStatus.CANCELLED);
        BigDecimal revenue = orderRepository.calculateTotalRevenue();

        stats.put("totalOrders", totalOrders);
        stats.put("pendingOrders", placed + confirmed);
        stats.put("shippedOrders", shipped);
        stats.put("deliveredOrders", delivered);
        stats.put("cancelledOrders", cancelled);
        stats.put("totalRevenue", revenue != null ? revenue : BigDecimal.ZERO);

        return stats;
    }
}
