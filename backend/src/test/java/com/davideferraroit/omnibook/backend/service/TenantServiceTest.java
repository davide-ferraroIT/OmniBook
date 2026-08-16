package com.davideferraroit.omnibook.backend.service;

import com.davideferraroit.omnibook.backend.dto.tenant.TenantCreateRequest;
import com.davideferraroit.omnibook.backend.dto.tenant.TenantResponse;
import com.davideferraroit.omnibook.backend.exception.ResourceNotFoundException;
import com.davideferraroit.omnibook.backend.exception.SlugAlreadyExistsException;
import com.davideferraroit.omnibook.backend.model.tenant.Tenant;
import com.davideferraroit.omnibook.backend.model.tenant.TenantRepository;
import com.davideferraroit.omnibook.backend.model.tenant.config.TenantConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantService tenantService;

    private Tenant tenant;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        tenant = Tenant.builder()
                .id(tenantId)
                .name("Test Tenant")
                .slug("test-tenant")
                .inviteCode("CODE123")
                .config(new TenantConfig("#000", null, null, null, null, null, null, false, null))
                .build();
    }

    @Test
    void findAll_ShouldReturnPageOfTenants() {
        Page<Tenant> page = new PageImpl<>(List.of(tenant));
        when(tenantRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<TenantResponse> result = tenantService.findAll(Pageable.unpaged());

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Test Tenant");
    }

    @Test
    void findById_ShouldReturnTenant_WhenExists() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        TenantResponse result = tenantService.findById(tenantId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(tenantId);
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantService.findById(tenantId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_ShouldSucceed_WhenSlugIsUnique() {
        TenantCreateRequest request = new TenantCreateRequest("Nuovo", "nuovo-slug", null);
        when(tenantRepository.findBySlug("nuovo-slug")).thenReturn(Optional.empty());
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        TenantResponse result = tenantService.create(request);

        assertThat(result).isNotNull();
        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void create_ShouldThrowException_WhenSlugExists() {
        TenantCreateRequest request = new TenantCreateRequest("Nuovo", "test-tenant", null);
        when(tenantRepository.findBySlug("test-tenant")).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> tenantService.create(request))
                .isInstanceOf(SlugAlreadyExistsException.class);
        
        verify(tenantRepository, never()).save(any(Tenant.class));
    }
    
    @Test
    void updateInviteCode_ShouldThrowException_WhenCodeUsedByOtherTenant() {
        Tenant otherTenant = Tenant.builder().id(UUID.randomUUID()).build();
        
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantRepository.findByInviteCode("USED_CODE")).thenReturn(Optional.of(otherTenant));
        
        assertThatThrownBy(() -> tenantService.updateInviteCode(tenantId, "USED_CODE"))
                .isInstanceOf(SlugAlreadyExistsException.class)
                .hasMessageContaining("già in uso da un altro negozio");
    }
    
    @Test
    void updateInviteCode_ShouldSucceed_WhenCodeIsUnique() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantRepository.findByInviteCode("NEW_CODE")).thenReturn(Optional.empty());
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        
        TenantResponse result = tenantService.updateInviteCode(tenantId, "NEW_CODE");
        
        assertThat(result).isNotNull();
        verify(tenantRepository).save(any(Tenant.class));
    }
}
