package com.davideferraroit.omnibook.backend.controller;

import com.davideferraroit.omnibook.backend.dto.tenant.TenantCreateRequest;
import com.davideferraroit.omnibook.backend.dto.tenant.TenantResponse;
import com.davideferraroit.omnibook.backend.exception.ResourceNotFoundException;
import com.davideferraroit.omnibook.backend.exception.SlugAlreadyExistsException;
import com.davideferraroit.omnibook.backend.model.tenant.config.FeatureModule;
import com.davideferraroit.omnibook.backend.model.tenant.config.TenantConfig;
import com.davideferraroit.omnibook.backend.model.tenant.config.Terminology;
import com.davideferraroit.omnibook.backend.service.TenantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TenantController.class)
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TenantService tenantService;

    @Test
    void shouldCreateTenantSuccessfully() throws Exception {
        TenantConfig config = new TenantConfig(
                "#000000",
                new Terminology("Risorsa", "Servizio", "Prenotazione"),
                List.of(),
                Set.of(FeatureModule.ONLINE_PAYMENTS),
                null,
                null,
                null,
                false,
                List.of()
        );

        TenantCreateRequest request = new TenantCreateRequest("Test Business", "test-slug", config);
        
        TenantResponse response = new TenantResponse(UUID.randomUUID(), "Test Business", "test-slug", config);

        when(tenantService.create(any(TenantCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Business"))
                .andExpect(jsonPath("$.slug").value("test-slug"));
    }

    @Test
    void shouldReturn409WhenSlugExists() throws Exception {
        TenantConfig config = new TenantConfig("#000000", new Terminology("R", "S", "P"), List.of(), Set.of(), null, null, null, false, java.util.List.of());
        TenantCreateRequest request = new TenantCreateRequest("Duplicate", "dup-slug", config);

        when(tenantService.create(any(TenantCreateRequest.class)))
                .thenThrow(new SlugAlreadyExistsException("Lo slug 'dup-slug' è già in uso."));

        mockMvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Lo slug 'dup-slug' è già in uso."));
    }

    @Test
    void shouldReturn404WhenTenantNotFound() throws Exception {
        when(tenantService.findBySlug("not-found"))
                .thenThrow(new ResourceNotFoundException("Tenant non trovato con slug: not-found"));

        mockMvc.perform(get("/api/v1/tenants/slug/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Tenant non trovato con slug: not-found"));
    }

    @Test
    void shouldReturn400WhenValidationFails() throws Exception {
        // Missing name and config, invalid slug
        TenantCreateRequest request = new TenantCreateRequest("", "INVALID SLUG!", null);

        mockMvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }
}
