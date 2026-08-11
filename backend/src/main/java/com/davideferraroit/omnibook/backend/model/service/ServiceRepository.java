package com.davideferraroit.omnibook.backend.model.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {
    Page<Service> findByTenantId(UUID tenantId, Pageable pageable);
    Optional<Service> findByIdAndTenantId(UUID id, UUID tenantId);
}
