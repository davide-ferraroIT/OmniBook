package com.davideferraroit.omnibook.backend.model.resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {
    Page<Resource> findByTenantId(UUID tenantId, Pageable pageable);
    Optional<Resource> findByIdAndTenantId(UUID id, UUID tenantId);
}
