package com.petstore.order.service;

import com.petstore.order.client.ProductCatalogClient;
import com.petstore.order.client.UserServiceClient;
import com.petstore.order.dto.*;
import com.petstore.order.entity.*;
import com.petstore.order.exception.BadRequestException;
import com.petstore.order.outbox.OutboxService;
import com.petstore.order.repository.CartRepository;
import com.petstore.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ProductCatalogClient productCatalogClient;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private OrderService orderService;

    private AddressDto sampleAddress;
    private Cart sampleCart;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleAddress = new AddressDto();
        sampleAddress.setId(50L);
        sampleAddress.setUserId(1L);
        sampleAddress.setName("Chetan Sharma");
        sampleAddress.setPhone("9876543210");
        sampleAddress.setAddressLine1("123 Indiranagar");
        sampleAddress.setCity("Bengaluru");
        sampleAddress.setState("Karnataka");
        sampleAddress.setPincode("560038");

        sampleCart = new Cart(1L);
        sampleCart.setId(10L);
        CartItem cartItem = new CartItem(sampleCart, 101L, 2);
        cartItem.setId(20L);
        sampleCart.setItems(new ArrayList<>(Collections.singletonList(cartItem)));

        sampleOrder = new Order(1L, new BigDecimal("5700.00"), OrderStatus.PLACED, PaymentMethod.COD, PaymentStatus.PENDING,
                "Chetan Sharma", "9876543210", "123 Indiranagar", null, "Bengaluru", "Karnataka", "560038");
        sampleOrder.setId(1001L);
        OrderItem orderItem = new OrderItem(sampleOrder, 101L, "Royal Canin Maxi", 2, new BigDecimal("2850.00"), new BigDecimal("5700.00"));
        sampleOrder.addItem(orderItem);
    }

    @Test
    @DisplayName("Should successfully place a COD order and record OrderCreated event in outbox")
    void shouldPlaceOrderSuccessfully() {
        CreateOrderRequest request = new CreateOrderRequest(50L, PaymentMethod.COD);
        ProductDto product = new ProductDto();
        product.setId(101L);
        product.setName("Royal Canin Maxi");
        product.setPrice(new BigDecimal("2850.00"));
        product.setStatus("ACTIVE");

        when(userServiceClient.getAddress(50L, 1L)).thenReturn(sampleAddress);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(sampleCart));
        when(productCatalogClient.getProduct(101L)).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(1001L);
            return o;
        });

        OrderResponse response = orderService.createOrder(1L, request);

        assertNotNull(response);
        assertEquals(1001L, response.getId());
        assertEquals(OrderStatus.PLACED, response.getOrderStatus());
        assertEquals(PaymentMethod.COD, response.getPaymentMethod());
        assertEquals(new BigDecimal("5700.00"), response.getTotalAmount());
        assertEquals("Chetan Sharma", response.getShippingName());
        verify(cartService, times(1)).clearCart(1L);
        verify(outboxService, times(1)).recordEvent(eq("OrderCreated"), eq("1001"), eq("ORDER"), eq(1), any(), any());
    }

    @Test
    @DisplayName("Should reject checkout when cart is empty")
    void shouldRejectCheckoutWhenCartEmpty() {
        sampleCart.setItems(new ArrayList<>());
        CreateOrderRequest request = new CreateOrderRequest(50L, PaymentMethod.COD);

        when(userServiceClient.getAddress(50L, 1L)).thenReturn(sampleAddress);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(sampleCart));

        assertThrows(BadRequestException.class, () -> orderService.createOrder(1L, request));
        verify(outboxService, never()).recordEvent(any(), any(), any(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("Should cancel eligible order and publish OrderCancelled outbox event")
    void shouldCancelOrderAndPublishEvent() {
        when(orderRepository.findByIdAndUserId(1001L, 1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.cancelOrder(1001L, 1L);

        assertNotNull(response);
        assertEquals(OrderStatus.CANCELLED, response.getOrderStatus());
        verify(outboxService, times(1)).recordEvent(eq("OrderCancelled"), eq("1001"), eq("ORDER"), eq(1), any(), any());
    }

    @Test
    @DisplayName("Should update order status for admin and publish OrderConfirmed outbox event")
    void shouldUpdateOrderStatusForAdmin() {
        when(orderRepository.findById(1001L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.updateOrderStatus(1001L, OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, response.getOrderStatus());
        verify(outboxService, times(1)).recordEvent(eq("OrderConfirmed"), eq("1001"), eq("ORDER"), eq(1), any(), any());
    }

    @Test
    @DisplayName("Should reject invalid order status transitions")
    void shouldRejectInvalidStatusTransition() {
        sampleOrder.setOrderStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1001L)).thenReturn(Optional.of(sampleOrder));

        assertThrows(BadRequestException.class, () -> orderService.updateOrderStatus(1001L, OrderStatus.PLACED));
    }
}
