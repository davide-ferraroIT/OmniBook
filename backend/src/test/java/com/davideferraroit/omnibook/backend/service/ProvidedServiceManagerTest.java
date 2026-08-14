package com.davideferraroit.omnibook.backend.service;

import com.davideferraroit.omnibook.backend.dto.service.ServiceCreateRequest;
import com.davideferraroit.omnibook.backend.dto.service.ServiceResponse;
import com.davideferraroit.omnibook.backend.exception.ResourceNotFoundException;
import com.davideferraroit.omnibook.backend.model.resource.Resource;
import com.davideferraroit.omnibook.backend.model.resource.ResourceRepository;
import com.davideferraroit.omnibook.backend.model.resource.ResourceType;
import com.davideferraroit.omnibook.backend.model.service.ServiceRepository;
import com.davideferraroit.omnibook.backend.model.tenant.Tenant;
import com.davideferraroit.omnibook.backend.model.tenant.TenantRepository;
import com.davideferraroit.omnibook.backend.model.tenant.config.TenantConfig;
import com.davideferraroit.omnibook.backend.model.tenant.config.Terminology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class ProvidedServiceManagerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ProvidedServiceManager providedServiceManager;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    private Tenant tenant1;
    private Tenant tenant2;
    private Resource resource1Tenant1;
    private Resource resource2Tenant2;

    @BeforeEach
    void setUp() {
        serviceRepository.deleteAll();
        resourceRepository.deleteAll();
        tenantRepository.deleteAll();

        TenantConfig config = new TenantConfig("#ffffff", new Terminology("R", "S", "P"), List.of(), Set.of(), null, null, null, false);
        tenant1 = tenantRepository.save(Tenant.builder().name("Tenant 1").slug("tenant-1").config(config).build());
        tenant2 = tenantRepository.save(Tenant.builder().name("Tenant 2").slug("tenant-2").config(config).build());

        resource1Tenant1 = resourceRepository.save(Resource.builder().tenant(tenant1).name("Poltrona 1").type(ResourceType.EQUIPMENT).capacity(1).build());
        resource2Tenant2 = resourceRepository.save(Resource.builder().tenant(tenant2).name("Poltrona Esterna").type(ResourceType.EQUIPMENT).capacity(1).build());
    }

    @Test
    void shouldCreateServiceWhenResourcesBelongToSameTenant() {
        ServiceCreateRequest request = new ServiceCreateRequest(
                "Taglio",
                30,
                Set.of(resource1Tenant1.getId()),
                null
        );
        
        ServiceResponse response = providedServiceManager.create(tenant1.getId(), request);
        
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Taglio");
        assertThat(response.allowedResources()).hasSize(1);
    }

    @Test
    void shouldThrowIdorExceptionWhenUsingResourceOfAnotherTenant() {
        // Tentativo di creare un servizio sul Tenant 1 usando una risorsa del Tenant 2
        ServiceCreateRequest request = new ServiceCreateRequest("Hacking Service", 30, Set.of(resource2Tenant2.getId()), null);
        
        assertThatThrownBy(() -> providedServiceManager.create(tenant1.getId(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("non trovata o non appartenente al tenant");
    }
}
