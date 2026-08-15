package com.davideferraroit.omnibook.backend.model.booking;

import com.davideferraroit.omnibook.backend.model.resource.Resource;
import com.davideferraroit.omnibook.backend.model.service.Service;
import com.davideferraroit.omnibook.backend.model.tenant.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.davideferraroit.omnibook.backend.model.common.BaseEntity;

@Entity
@Table(name = "bookings", indexes = {
    @Index(name = "idx_booking_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_booking_service_id", columnList = "service_id"),
    @Index(name = "idx_booking_resource_id", columnList = "resource_id"),
    @Index(name = "idx_booking_start_time", columnList = "startTime")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @ManyToOne(optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerEmail;

    private String customerPhone;
}
