package com.davideferraroit.omnibook.backend.service;

import com.davideferraroit.omnibook.backend.dto.service.ServiceCreateRequest;
import com.davideferraroit.omnibook.backend.dto.service.ServiceResponse;
import com.davideferraroit.omnibook.backend.exception.ResourceNotFoundException;
import com.davideferraroit.omnibook.backend.model.resource.Resource;
import com.davideferraroit.omnibook.backend.model.resource.ResourceRepository;
import com.davideferraroit.omnibook.backend.model.service.ServiceRepository;
import com.davideferraroit.omnibook.backend.model.tenant.Tenant;
import com.davideferraroit.omnibook.backend.model.tenant.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProvidedServiceManager {

    private final ServiceRepository serviceRepository;
    private final TenantRepository tenantRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceService resourceService;

    @Transactional(readOnly = true)
    public Page<ServiceResponse> findAllByTenant(UUID tenantId, Pageable pageable) {
        log.debug("Recupero servizi offerti per il tenant: {}", tenantId);
        return serviceRepository.findByTenantId(tenantId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ServiceResponse findByIdAndTenantId(UUID id, UUID tenantId) {
        log.debug("Recupero servizio offerto ID: {} per tenant: {}", id, tenantId);
        return serviceRepository.findByIdAndTenantId(id, tenantId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Servizio non trovato o non appartenente a questo tenant."));
    }

    @Transactional
    public ServiceResponse create(UUID tenantId, ServiceCreateRequest request) {
        log.debug("Creazione nuovo servizio offerto per il tenant: {}", tenantId);
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant non trovato con ID: " + tenantId));

        Set<Resource> allowedResources = new HashSet<>();
        for (UUID resourceId : request.allowedResourceIds()) {
            Resource resource = resourceRepository.findByIdAndTenantId(resourceId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Risorsa con ID " + resourceId + " non trovata o non appartenente al tenant."));
            allowedResources.add(resource);
        }

        com.davideferraroit.omnibook.backend.model.service.Service service = 
            com.davideferraroit.omnibook.backend.model.service.Service.builder()
                .tenant(tenant)
                .name(request.name())
                .durationMinutes(request.durationMinutes())
                .allowedResources(allowedResources)
                .build();

        com.davideferraroit.omnibook.backend.model.service.Service saved = serviceRepository.save(service);
        return mapToResponse(saved);
    }

    @Transactional
    public void delete(UUID id, UUID tenantId) {
        log.debug("Cancellazione servizio offerto ID: {} per tenant: {}", id, tenantId);
        
        com.davideferraroit.omnibook.backend.model.service.Service service = serviceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Servizio non trovato o non appartenente a questo tenant."));
                
        serviceRepository.delete(service);
    }

    private ServiceResponse mapToResponse(com.davideferraroit.omnibook.backend.model.service.Service service) {
        return new ServiceResponse(
                service.getId(),
                service.getName(),
                service.getDurationMinutes(),
                service.getAllowedResources().stream()
                        .map(resourceService::mapToResponse)
                        .collect(Collectors.toSet())
        );
    }
}
