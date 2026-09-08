package com.petstore.inventory.repository;

import com.petstore.inventory.entity.InventoryReservation;
import com.petstore.inventory.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

    List<InventoryReservation> findByOrderId(Long orderId);

    List<InventoryReservation> findByOrderIdAndStatus(Long orderId, ReservationStatus status);
}
