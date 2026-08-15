package com.davideferraroit.omnibook.backend.model.resource;

import com.davideferraroit.omnibook.backend.model.tenant.Tenant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.UUID;

import com.davideferraroit.omnibook.backend.model.common.BaseEntity;

@Entity
@Table(name = "resources", indexes = {
    @Index(name = "idx_resource_tenant_id", columnList = "tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int capacity; // 1 for Barber Chair, 20 for Yoga Room

    @Column(name = "image_url")
    private String imageUrl;
}
