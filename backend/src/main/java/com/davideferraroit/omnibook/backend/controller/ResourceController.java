package com.davideferraroit.omnibook.backend.controller;

import com.davideferraroit.omnibook.backend.dto.resource.ResourceCreateRequest;
import com.davideferraroit.omnibook.backend.dto.resource.ResourceResponse;
import com.davideferraroit.omnibook.backend.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping
    public ResponseEntity<ResourceResponse> createResource(
            @PathVariable UUID tenantId,
            @Valid @RequestBody ResourceCreateRequest request) {
        log.info("Ricevuta richiesta REST per creazione risorsa per tenant: {}", tenantId);
        ResourceResponse response = resourceService.create(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ResourceResponse>> getAllResources(
            @PathVariable UUID tenantId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.info("Ricevuta richiesta REST per paginazione risorse tenant: {}", tenantId);
        return ResponseEntity.ok(resourceService.findAllByTenant(tenantId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> getResourceById(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        log.info("Ricevuta richiesta REST per recupero risorsa ID: {} (tenant: {})", id, tenantId);
        return ResponseEntity.ok(resourceService.findByIdAndTenantId(id, tenantId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        log.info("Ricevuta richiesta REST per eliminazione risorsa ID: {} (tenant: {})", id, tenantId);
        resourceService.delete(id, tenantId);
        return ResponseEntity.noContent().build();
    }
}
