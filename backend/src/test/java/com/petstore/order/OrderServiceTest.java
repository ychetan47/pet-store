package com.petstore.order;

import com.petstore.address.entity.Address;
import com.petstore.address.repository.AddressRepository;
import com.petstore.auth.entity.User;
import com.petstore.auth.repository.UserRepository;
import com.petstore.cart.entity.Cart;
import com.petstore.cart.entity.CartItem;
import com.petstore.cart.repository.CartItemRepository;
import com.petstore.cart.repository.CartRepository;
import com.petstore.common.exception.BadRequestException;
import com.petstore.common.exception.InsufficientStockException;
import com.petstore.common.exception.InvalidOrderStatusException;
import com.petstore.inventory.service.InventoryService;
import com.petstore.order.dto.CreateOrderRequest;
import com.petstore.order.dto.OrderResponse;
import com.petstore.order.entity.*;
import com.petstore.order.repository.OrderItemRepository;
import com.petstore.order.repository.OrderRepository;
import com.petstore.order.service.OrderService;
import com.petstore.product.entity.Product;
import com.petstore.product.entity.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Address address;
    private Cart cart;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        user = new User("Alice", "alice@example.com", "secret", "9876543210");
        user.setId(1L);

        address = new Address(user, "Alice", "9876543210", "123 Pet St", "Apt 4B", "Bangalore", "Karnataka", "560001", true);
        address.setId(10L);

        cart = new Cart(user);
        cart.setId(100L);

        product = new Product("Pedigree Pro Adult", "pedigree-pro", "Food", "Pedigree",
                BigDecimal.valueOf(1450), 20, null, "3kg", ProductStatus.ACTIVE);
        product.setId(200L);

        cartItem = new CartItem(cart, product, 2);
        cartItem.setId(300L);
    }

    @Test
    void createOrder_validCOD_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(address));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(100L)).thenReturn(Collections.singletonList(cartItem));

        when(inventoryService.lockAndDeductStock(200L, 2)).thenReturn(product);

        Order savedOrder = new Order(user, BigDecimal.valueOf(2900), OrderStatus.PLACED,
                PaymentMethod.COD, PaymentStatus.PENDING, "Alice, Phone: 9876543210, 123 Pet St, Apt 4B, Bangalore, Karnataka - 560001");
        savedOrder.setId(500L);
        savedOrder.addItem(new OrderItem(savedOrder, product, "Pedigree Pro Adult", 2, BigDecimal.valueOf(1450), BigDecimal.valueOf(2900)));

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        CreateOrderRequest request = new CreateOrderRequest(10L, PaymentMethod.COD);
        OrderResponse response = orderService.createOrder(1L, request);

        assertNotNull(response);
        assertEquals(500L, response.getId());
        assertEquals(OrderStatus.PLACED, response.getOrderStatus());
        assertEquals(PaymentMethod.COD, response.getPaymentMethod());
        assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
        assertEquals(BigDecimal.valueOf(2900), response.getTotalAmount());
        assertEquals(1, response.getItems().size());
        assertEquals("Pedigree Pro Adult", response.getItems().get(0).getProductName());

        verify(inventoryService).lockAndDeductStock(200L, 2);
        verify(cartItemRepository).deleteByCartId(100L);
    }

    @Test
    void createOrder_emptyCart_throwsBadRequestException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(address));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(100L)).thenReturn(Collections.emptyList());

        CreateOrderRequest request = new CreateOrderRequest(10L, PaymentMethod.COD);
        assertThrows(BadRequestException.class, () -> orderService.createOrder(1L, request));

        verify(inventoryService, never()).lockAndDeductStock(anyLong(), anyInt());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_invalidAddress_throwsBadRequestException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        CreateOrderRequest request = new CreateOrderRequest(999L, PaymentMethod.COD);
        assertThrows(BadRequestException.class, () -> orderService.createOrder(1L, request));
    }

    @Test
    void createOrder_insufficientStock_propagatesException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(address));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(100L)).thenReturn(Collections.singletonList(cartItem));

        when(inventoryService.lockAndDeductStock(200L, 2)).thenThrow(new InsufficientStockException(200L, 2, 1));

        CreateOrderRequest request = new CreateOrderRequest(10L, PaymentMethod.COD);
        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(1L, request));

        verify(orderRepository, never()).save(any());
        verify(cartItemRepository, never()).deleteByCartId(anyLong());
    }

    @Test
    void cancelOrder_placedOrder_successAndRestoresStock() {
        Order order = new Order(user, BigDecimal.valueOf(2900), OrderStatus.PLACED,
                PaymentMethod.COD, PaymentStatus.PENDING, "Alice");
        order.setId(500L);
        OrderItem item = new OrderItem(order, product, "Pedigree Pro Adult", 2, BigDecimal.valueOf(1450), BigDecimal.valueOf(2900));
        order.addItem(item);

        when(orderRepository.findByIdAndUserId(500L, 1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        OrderResponse response = orderService.cancelOrder(1L, 500L);

        assertEquals(OrderStatus.CANCELLED, response.getOrderStatus());
        verify(inventoryService).restoreStock(200L, 2);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_shippedOrder_throwsInvalidOrderStatusException() {
        Order order = new Order(user, BigDecimal.valueOf(2900), OrderStatus.SHIPPED,
                PaymentMethod.COD, PaymentStatus.PENDING, "Alice");
        order.setId(500L);

        when(orderRepository.findByIdAndUserId(500L, 1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusException.class, () -> orderService.cancelOrder(1L, 500L));
        verify(inventoryService, never()).restoreStock(anyLong(), anyInt());
    }
}
