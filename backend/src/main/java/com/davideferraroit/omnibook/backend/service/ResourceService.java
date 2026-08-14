package com.davideferraroit.omnibook.backend.service;

import com.davideferraroit.omnibook.backend.dto.resource.ResourceCreateRequest;
import com.davideferraroit.omnibook.backend.dto.resource.ResourceResponse;
import com.davideferraroit.omnibook.backend.exception.ResourceNotFoundException;
import com.davideferraroit.omnibook.backend.model.resource.Resource;
import com.davideferraroit.omnibook.backend.model.resource.ResourceRepository;
import com.davideferraroit.omnibook.backend.model.tenant.Tenant;
import com.davideferraroit.omnibook.backend.model.tenant.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public Page<ResourceResponse> findAllByTenant(UUID tenantId, Pageable pageable) {
        log.debug("Recupero risorse per il tenant: {}", tenantId);
        return resourceRepository.findByTenantId(tenantId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ResourceResponse findByIdAndTenantId(UUID id, UUID tenantId) {
        log.debug("Recupero risorsa ID: {} per tenant: {}", id, tenantId);
        return resourceRepository.findByIdAndTenantId(id, tenantId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Risorsa non trovata o non appartenente a questo tenant."));
    }

    @Transactional
    public ResourceResponse create(UUID tenantId, ResourceCreateRequest request) {
        log.debug("Creazione nuova risorsa per il tenant: {}", tenantId);
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant non trovato con ID: " + tenantId));

        Resource resource = Resource.builder()
                .tenant(tenant)
                .name(request.name())
                .capacity(request.capacity())
                .imageUrl(request.imageUrl())
                .build();

        Resource saved = resourceRepository.save(resource);
        return mapToResponse(saved);
    }

    @Transactional
    public ResourceResponse update(UUID tenantId, UUID resourceId, com.davideferraroit.omnibook.backend.dto.resource.ResourceUpdateRequest request) {
        log.debug("Aggiornamento risorsa ID: {} per il tenant: {}", resourceId, tenantId);
        
        Resource resource = resourceRepository.findByIdAndTenantId(resourceId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Risorsa non trovata o non appartenente a questo tenant."));

        resource.setName(request.name());
        resource.setCapacity(request.capacity());
        resource.setImageUrl(request.imageUrl());

        Resource saved = resourceRepository.save(resource);
        return mapToResponse(saved);
    }

    @Transactional
    public void delete(UUID id, UUID tenantId) {
        log.debug("Cancellazione risorsa ID: {} per tenant: {}", id, tenantId);
        
        Resource resource = resourceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Risorsa non trovata o non appartenente a questo tenant."));
                
        resourceRepository.delete(resource);
    }

    public ResourceResponse mapToResponse(Resource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getCapacity(),
                resource.getImageUrl()
        );
    }
}
