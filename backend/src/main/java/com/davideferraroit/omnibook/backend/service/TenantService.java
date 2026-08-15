package com.davideferraroit.omnibook.backend.service;

import com.davideferraroit.omnibook.backend.dto.tenant.TenantCreateRequest;
import com.davideferraroit.omnibook.backend.dto.tenant.TenantResponse;
import com.davideferraroit.omnibook.backend.exception.ResourceNotFoundException;
import com.davideferraroit.omnibook.backend.exception.SlugAlreadyExistsException;
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
public class TenantService {

    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public Page<TenantResponse> findAll(Pageable pageable) {
        log.debug("Recupero tutti i tenant con paginazione: {}", pageable);
        return tenantRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public TenantResponse findById(UUID id) {
        log.debug("Recupero tenant per ID: {}", id);
        return tenantRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant non trovato con ID: " + id));
    }

    @Transactional(readOnly = true)
    public TenantResponse findBySlug(String slug) {
        log.debug("Recupero tenant per slug: {}", slug);
        return tenantRepository.findBySlug(slug)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant non trovato con slug: " + slug));
    }

    @Transactional(readOnly = true)
    public Tenant findEntityByInviteCode(String inviteCode) {
        log.debug("Recupero entity tenant per inviteCode: {}", inviteCode);
        return tenantRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun negozio trovato con il codice invito fornito."));
    }

    @Transactional
    public TenantResponse create(TenantCreateRequest request) {
        log.debug("Creazione nuovo tenant con slug: {}", request.slug());
        
        if (tenantRepository.findBySlug(request.slug()).isPresent()) {
            throw new SlugAlreadyExistsException("Lo slug '" + request.slug() + "' è già in uso.");
        }

        Tenant tenant = Tenant.builder()
                .name(request.name())
                .slug(request.slug())
                .config(request.config())
                .build();

        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("Tenant creato con successo con ID: {}", savedTenant.getId());
        
        return mapToResponse(savedTenant);
    }

    @Transactional
    public TenantResponse updateConfig(UUID id, com.davideferraroit.omnibook.backend.model.tenant.config.TenantConfig config) {
        log.debug("Aggiornamento configurazione tenant ID: {}", id);
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant non trovato con ID: " + id));
        
        tenant.setConfig(config);
        Tenant savedTenant = tenantRepository.save(tenant);
        
        log.info("Configurazione tenant aggiornata con successo per ID: {}", id);
        return mapToResponse(savedTenant);
    }

    @Transactional
    public TenantResponse updateInviteCode(UUID id, String inviteCode) {
        log.debug("Aggiornamento invite code tenant ID: {}", id);
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant non trovato con ID: " + id));
        
        // Verifica univocità se il codice è nuovo e già in uso
        if (tenantRepository.findByInviteCode(inviteCode).filter(t -> !t.getId().equals(id)).isPresent()) {
             throw new SlugAlreadyExistsException("Il codice invito '" + inviteCode + "' è già in uso da un altro negozio.");
        }

        tenant.setInviteCode(inviteCode);
        Tenant savedTenant = tenantRepository.save(tenant);
        
        log.info("Invite code aggiornato con successo per ID: {}", id);
        return mapToResponse(savedTenant);
    }

    private TenantResponse mapToResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getInviteCode(),
                tenant.getConfig()
        );
    }
}
