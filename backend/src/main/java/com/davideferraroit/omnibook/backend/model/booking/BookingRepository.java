package com.davideferraroit.omnibook.backend.model.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    
    Page<Booking> findByTenantId(UUID tenantId, Pageable pageable);
    
    Optional<Booking> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.resource.id = :resourceId AND b.status != 'CANCELED' AND b.startTime < :endTime AND b.endTime > :startTime")
    long countOverlappingBookings(@Param("resourceId") UUID resourceId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
