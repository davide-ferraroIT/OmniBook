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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ProvidedServiceManager providedServiceManager;
    private final com.davideferraroit.omnibook.backend.service.CloudinaryService cloudinaryService;

    @PreAuthorize("hasRole('ADMIN') or (hasRole('SHOP') and principal.tenant != null and principal.tenant.id == #tenantId)")
    @PostMapping(value = "/upload-image", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<java.util.Map<String, String>> uploadImage(
            @PathVariable UUID tenantId,
            @RequestPart("file") org.springframework.web.multipart.MultipartFile file) {
        log.info("Ricevuta richiesta REST per upload immagine servizio per tenant: {}", tenantId);
        try {
            String url = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(java.util.Map.of("imageUrl", url));
        } catch (java.io.IOException e) {
            log.error("Errore durante l'upload dell'immagine", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('SHOP') and principal.tenant != null and principal.tenant.id == #tenantId)")
    @PostMapping
    public ResponseEntity<ServiceResponse> createService(
            @PathVariable UUID tenantId,
            @Valid @RequestBody ServiceCreateRequest request) {
        log.info("Ricevuta richiesta REST per creazione servizio offerto per tenant: {}", tenantId);
        ServiceResponse response = providedServiceManager.create(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('SHOP') and principal.tenant != null and principal.tenant.id == #tenantId)")
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

    @PreAuthorize("hasRole('ADMIN') or (hasRole('SHOP') and principal.tenant != null and principal.tenant.id == #tenantId)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        log.info("Ricevuta richiesta REST per eliminazione servizio offerto ID: {} (tenant: {})", id, tenantId);
        providedServiceManager.delete(id, tenantId);
        return ResponseEntity.noContent().build();
    }
}
