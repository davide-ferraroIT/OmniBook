package com.davideferraroit.omnibook.backend.model.tenant;

import com.davideferraroit.omnibook.backend.model.tenant.config.FeatureModule;
import com.davideferraroit.omnibook.backend.model.tenant.config.TenantConfig;
import com.davideferraroit.omnibook.backend.model.tenant.config.Terminology;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class TenantRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    void shouldSaveAndRetrieveTenantWithJsonbConfig() {
        // Arrange
        TenantConfig config = new TenantConfig(
                "#FF0000",
                new Terminology("Ponte", "Intervento", "Prenotazione"),
                List.of(),
                Set.of(FeatureModule.ONLINE_PAYMENTS, FeatureModule.SMS_REMINDERS),
                null,
                null,
                null,
                false,
                List.of()
        );

        Tenant tenant = Tenant.builder()
                .name("Gommista Rossi")
                .slug("gommista-rossi")
                .config(config)
                .build();

        // Act
        Tenant savedTenant = tenantRepository.saveAndFlush(tenant);
        
        // Assert
        assertThat(savedTenant.getId()).isNotNull();
        
        // Clear persistence context to force a read from database
        tenantRepository.flush();
        
        Tenant retrievedTenant = tenantRepository.findById(savedTenant.getId()).orElseThrow();
        
        assertThat(retrievedTenant.getSlug()).isEqualTo("gommista-rossi");
        assertThat(retrievedTenant.getConfig()).isNotNull();
        assertThat(retrievedTenant.getConfig().primaryColor()).isEqualTo("#FF0000");
        assertThat(retrievedTenant.getConfig().activeModules())
                .containsExactlyInAnyOrder(FeatureModule.ONLINE_PAYMENTS, FeatureModule.SMS_REMINDERS);
    }
}
