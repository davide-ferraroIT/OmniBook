package com.davideferraroit.omnibook.backend.model.service;

import com.davideferraroit.omnibook.backend.model.resource.Resource;
import com.davideferraroit.omnibook.backend.model.tenant.Tenant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.davideferraroit.omnibook.backend.model.common.BaseEntity;

@Entity
@Table(name = "services", indexes = {
    @Index(name = "idx_service_tenant_id", columnList = "tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int durationMinutes;

    @ManyToMany
    @JoinTable(
        name = "service_resources",
        joinColumns = @JoinColumn(name = "service_id"),
        inverseJoinColumns = @JoinColumn(name = "resource_id")
    )
    @Builder.Default
    private Set<Resource> allowedResources = new HashSet<>();

    @Column(columnDefinition = "TEXT")
    private String imageUrl;
}
