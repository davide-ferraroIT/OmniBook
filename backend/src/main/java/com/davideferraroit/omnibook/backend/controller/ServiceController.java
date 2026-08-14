package com.davideferraroit.omnibook.backend.controller;

import com.davideferraroit.omnibook.backend.dto.service.ServiceCreateRequest;
import com.davideferraroit.omnibook.backend.dto.service.ServiceResponse;
import com.davideferraroit.omnibook.backend.service.ProvidedServiceManager;
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
@RequestMapping("/api/v1/tenants/{tenantId}/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ProvidedServiceManager providedServiceManager;

    @PostMapping
    public ResponseEntity<ServiceResponse> createService(
            @PathVariable UUID tenantId,
            @Valid @RequestBody ServiceCreateRequest request) {
        log.info("Ricevuta richiesta REST per creazione servizio offerto per tenant: {}", tenantId);
        ServiceResponse response = providedServiceManager.create(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody ServiceCreateRequest request) {
        log.info("Ricevuta richiesta REST per modifica servizio offerto ID: {} per tenant: {}", id, tenantId);
        ServiceResponse response = providedServiceManager.update(id, tenantId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ServiceResponse>> getAllServices(
            @PathVariable UUID tenantId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.info("Ricevuta richiesta REST per paginazione servizi offerti tenant: {}", tenantId);
        return ResponseEntity.ok(providedServiceManager.findAllByTenant(tenantId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        log.info("Ricevuta richiesta REST per recupero servizio offerto ID: {} (tenant: {})", id, tenantId);
        return ResponseEntity.ok(providedServiceManager.findByIdAndTenantId(id, tenantId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        log.info("Ricevuta richiesta REST per eliminazione servizio offerto ID: {} (tenant: {})", id, tenantId);
        providedServiceManager.delete(id, tenantId);
        return ResponseEntity.noContent().build();
    }
}
