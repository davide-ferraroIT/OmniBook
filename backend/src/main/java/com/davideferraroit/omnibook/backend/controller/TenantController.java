package com.davideferraroit.omnibook.backend.controller;

import com.davideferraroit.omnibook.backend.dto.tenant.TenantCreateRequest;
import com.davideferraroit.omnibook.backend.dto.tenant.TenantResponse;
import com.davideferraroit.omnibook.backend.service.TenantService;
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
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody TenantCreateRequest request) {
        log.info("Ricevuta richiesta REST per creazione tenant: {}", request.slug());
        TenantResponse response = tenantService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<TenantResponse>> getAllTenants(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.info("Ricevuta richiesta REST per paginazione tenant: {}", pageable);
        return ResponseEntity.ok(tenantService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable UUID id) {
        log.info("Ricevuta richiesta REST per recupero tenant ID: {}", id);
        return ResponseEntity.ok(tenantService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<TenantResponse> getTenantBySlug(@PathVariable String slug) {
        log.info("Ricevuta richiesta REST per recupero tenant Slug: {}", slug);
        return ResponseEntity.ok(tenantService.findBySlug(slug));
    }
}
