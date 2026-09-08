package com.petstore.inventory.service;

import com.petstore.inventory.dto.InventoryItemDto;
import com.petstore.inventory.entity.InventoryItem;
import com.petstore.inventory.entity.InventoryReservation;
import com.petstore.inventory.entity.ReservationStatus;
import com.petstore.inventory.event.OrderCreatedEvent;
import com.petstore.inventory.outbox.OutboxService;
import com.petstore.inventory.repository.InventoryItemRepository;
import com.petstore.inventory.repository.InventoryReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private InventoryReservationRepository reservationRepository;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private InventoryService inventoryService;

    private InventoryItem testItem;

    @BeforeEach
    void setUp() {
        testItem = new InventoryItem(101L, 20);
        testItem.setReservedQuantity(0);
    }

    @Test
    void testReserveStock_Success() {
        when(inventoryItemRepository.findByProductIdForUpdate(101L))
                .thenReturn(Optional.of(testItem));

        OrderCreatedEvent.OrderItemPayload itemPayload = new OrderCreatedEvent.OrderItemPayload(
                101L, "Dog Food", 5, new BigDecimal("500.00"), new BigDecimal("2500.00")
        );

        boolean result = inventoryService.reserveStock(1001L, List.of(itemPayload), "test-corr-id");

        assertTrue(result);
        assertEquals(15, testItem.getAvailableQuantity());
        assertEquals(5, testItem.getReservedQuantity());

        verify(reservationRepository).save(any(InventoryReservation.class));
        verify(outboxService).recordEvent(eq("InventoryReserved"), eq("1001"), eq("INVENTORY"), eq(1), eq("test-corr-id"), any());
    }

    @Test
    void testReserveStock_InsufficientStock() {
        when(inventoryItemRepository.findByProductIdForUpdate(101L))
                .thenReturn(Optional.of(testItem));

        // Request 25 units when available is 20
        OrderCreatedEvent.OrderItemPayload itemPayload = new OrderCreatedEvent.OrderItemPayload(
                101L, "Dog Food", 25, new BigDecimal("500.00"), new BigDecimal("12500.00")
        );

        boolean result = inventoryService.reserveStock(1001L, List.of(itemPayload), "test-corr-id");

        assertFalse(result);
        // Quantities should remain untouched
        assertEquals(20, testItem.getAvailableQuantity());
        assertEquals(0, testItem.getReservedQuantity());

        verify(outboxService).recordEvent(eq("InventoryReservationFailed"), eq("1001"), eq("INVENTORY"), eq(1), eq("test-corr-id"), any());
    }

    @Test
    void testReleaseStock() {
        InventoryReservation res = new InventoryReservation(1001L, 101L, 5, ReservationStatus.RESERVED);
        testItem.setAvailableQuantity(15);
        testItem.setReservedQuantity(5);

        when(reservationRepository.findByOrderIdAndStatus(1001L, ReservationStatus.RESERVED))
                .thenReturn(List.of(res));
        when(inventoryItemRepository.findByProductIdForUpdate(101L))
                .thenReturn(Optional.of(testItem));

        inventoryService.releaseStock(1001L, "test-corr-id");

        assertEquals(20, testItem.getAvailableQuantity());
        assertEquals(0, testItem.getReservedQuantity());
        assertEquals(ReservationStatus.RELEASED, res.getStatus());

        verify(reservationRepository).save(res);
        verify(outboxService).recordEvent(eq("InventoryReleased"), eq("1001"), eq("INVENTORY"), eq(1), eq("test-corr-id"), any());
    }

    @Test
    void testUpdateStock_Admin() {
        when(inventoryItemRepository.findByProductIdForUpdate(101L))
                .thenReturn(Optional.of(testItem));
        when(inventoryItemRepository.save(any(InventoryItem.class)))
                .thenAnswer(i -> i.getArgument(0));

        InventoryItemDto updated = inventoryService.updateStock(101L, 50);

        assertNotNull(updated);
        assertEquals(50, updated.getAvailableQuantity());
    }
}
