package com.davideferraroit.omnibook.backend.service;

import com.davideferraroit.omnibook.backend.dto.resource.ResourceCreateRequest;
import com.davideferraroit.omnibook.backend.dto.resource.ResourceResponse;
import com.davideferraroit.omnibook.backend.dto.resource.ResourceUpdateRequest;
import com.davideferraroit.omnibook.backend.exception.ResourceNotFoundException;
import com.davideferraroit.omnibook.backend.model.resource.Resource;
import com.davideferraroit.omnibook.backend.model.resource.ResourceRepository;
import com.davideferraroit.omnibook.backend.model.tenant.Tenant;
import com.davideferraroit.omnibook.backend.model.tenant.TenantRepository;
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
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private ResourceService resourceService;

    private UUID tenantId;
    private UUID resourceId;
    private Tenant tenant;
    private Resource resource;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        resourceId = UUID.randomUUID();
        tenant = Tenant.builder().id(tenantId).name("Tenant 1").build();
        resource = Resource.builder().id(resourceId).name("Poltrona").capacity(1).tenant(tenant).build();
    }

    @Test
    void findAllByTenant_ShouldReturnPage() {
        when(resourceRepository.findByTenantId(eq(tenantId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(resource)));

        Page<ResourceResponse> result = resourceService.findAllByTenant(tenantId, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Poltrona");
    }

    @Test
    void findByIdAndTenantId_ShouldReturnResource() {
        when(resourceRepository.findByIdAndTenantId(resourceId, tenantId)).thenReturn(Optional.of(resource));

        ResourceResponse result = resourceService.findByIdAndTenantId(resourceId, tenantId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(resourceId);
    }

    @Test
    void findByIdAndTenantId_ShouldThrowException_WhenNotFound() {
        when(resourceRepository.findByIdAndTenantId(resourceId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resourceService.findByIdAndTenantId(resourceId, tenantId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_ShouldSucceed() {
        ResourceCreateRequest request = new ResourceCreateRequest("Nuova", 2, null);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(resourceRepository.save(any(Resource.class))).thenReturn(resource);

        ResourceResponse result = resourceService.create(tenantId, request);

        assertThat(result).isNotNull();
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    void update_ShouldSucceed() {
        ResourceUpdateRequest request = new ResourceUpdateRequest("Modificata", 3, null);
        when(resourceRepository.findByIdAndTenantId(resourceId, tenantId)).thenReturn(Optional.of(resource));
        when(resourceRepository.save(any(Resource.class))).thenReturn(resource);

        ResourceResponse result = resourceService.update(tenantId, resourceId, request);

        assertThat(result).isNotNull();
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    void delete_ShouldSucceed() {
        when(resourceRepository.findByIdAndTenantId(resourceId, tenantId)).thenReturn(Optional.of(resource));

        resourceService.delete(resourceId, tenantId);

        verify(resourceRepository).delete(resource);
    }
}
