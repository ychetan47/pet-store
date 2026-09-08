package com.petstore.inventory.repository;

import com.petstore.inventory.entity.OutboxEvent;
import com.petstore.inventory.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    @Query(value = "SELECT * FROM outbox_events WHERE status = 'PENDING' ORDER BY created_at ASC LIMIT :limit FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<OutboxEvent> findPendingForUpdateSkipLocked(@Param("limit") int limit);

    List<OutboxEvent> findByStatus(OutboxStatus status);
}
