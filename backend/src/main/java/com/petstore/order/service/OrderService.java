package com.petstore.order.service;

import com.petstore.address.entity.Address;
import com.petstore.address.repository.AddressRepository;
import com.petstore.auth.entity.User;
import com.petstore.auth.repository.UserRepository;
import com.petstore.cart.entity.Cart;
import com.petstore.cart.entity.CartItem;
import com.petstore.cart.repository.CartItemRepository;
import com.petstore.cart.repository.CartRepository;
import com.petstore.common.exception.BadRequestException;
import com.petstore.common.exception.InvalidOrderStatusException;
import com.petstore.common.exception.ResourceNotFoundException;
import com.petstore.inventory.service.InventoryService;
import com.petstore.order.dto.CreateOrderRequest;
import com.petstore.order.dto.OrderResponse;
import com.petstore.order.dto.OrderSummaryDto;
import com.petstore.order.entity.*;
import com.petstore.order.repository.OrderItemRepository;
import com.petstore.order.repository.OrderRepository;
import com.petstore.product.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        AddressRepository addressRepository,
                        UserRepository userRepository,
                        InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
    }

    /**
     * Executes order checkout atomically inside a database transaction.
     * Validates customer, address, cart items, deducts inventory with pessimistic locking,
     * snapshots line items, clears cart, and confirms order.
     */
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Address address = addressRepository.findByIdAndUserId(request.getAddressId(), userId)
                .orElseThrow(() -> new BadRequestException("Invalid delivery address selected"));

        String shippingAddressSnapshot = formatAddressSnapshot(address);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("No active cart found for checkout"));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cannot checkout with an empty cart");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = new Order(
                user,
                BigDecimal.ZERO,
                OrderStatus.PLACED,
                PaymentMethod.COD,
                PaymentStatus.PENDING,
                shippingAddressSnapshot
        );

        for (CartItem cartItem : cartItems) {
            Product currentProduct = cartItem.getProduct();

            // Pessimistic lock and inventory reduction
            Product lockedProduct = inventoryService.lockAndDeductStock(currentProduct.getId(), cartItem.getQuantity());

            BigDecimal itemPrice = lockedProduct.getPrice();
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = new OrderItem(
                    order,
                    lockedProduct,
                    lockedProduct.getName(),
                    cartItem.getQuantity(),
                    itemPrice,
                    itemTotal
            );
            orderItems.add(orderItem);
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Clear customer cart
        cartItemRepository.deleteByCartId(cart.getId());

        logger.info("Order placed successfully. Order ID: {}, User: {}, Total: ₹{}",
                savedOrder.getId(), userId, savedOrder.getTotalAmount());

        return OrderResponse.fromEntity(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryDto> getCustomerOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderSummaryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getOrderStatus().canCancel()) {
            throw new InvalidOrderStatusException(
                    "Order #" + orderId + " cannot be cancelled because its current status is: " + order.getOrderStatus()
            );
        }

        // Restock inventory for each item
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() != null) {
                inventoryService.restoreStock(item.getProduct().getId(), item.getQuantity());
            }
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        logger.info("Order #{} cancelled by user {}. Inventory restored.", orderId, userId);
        return OrderResponse.fromEntity(updatedOrder);
    }

    private String formatAddressSnapshot(Address address) {
        StringBuilder sb = new StringBuilder();
        sb.append(address.getName()).append(", ");
        sb.append("Phone: ").append(address.getPhone()).append(", ");
        sb.append(address.getAddressLine1());
        if (address.getAddressLine2() != null && !address.getAddressLine2().isBlank()) {
            sb.append(", ").append(address.getAddressLine2());
        }
        sb.append(", ").append(address.getCity()).append(", ");
        sb.append(address.getState()).append(" - ").append(address.getPincode());
        return sb.toString();
    }
}
