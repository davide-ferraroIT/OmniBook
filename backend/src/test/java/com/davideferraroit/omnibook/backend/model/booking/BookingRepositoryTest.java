package com.davideferraroit.omnibook.backend.model.booking;

import com.davideferraroit.omnibook.backend.model.resource.Resource;
import com.davideferraroit.omnibook.backend.model.service.Service;
import com.davideferraroit.omnibook.backend.model.tenant.Tenant;
import com.davideferraroit.omnibook.backend.model.tenant.config.TenantConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class BookingRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager testEntityManager;

    private Tenant tenant;
    private Resource resource;
    private Service service;

    @BeforeEach
    void setUp() {
        tenant = Tenant.builder()
                .name("Tenant")
                .slug("tenant-repo-test")
                .config(new TenantConfig("#000", null, null, null, null, null, null, false, null))
                .build();
        testEntityManager.persistAndFlush(tenant);

        resource = Resource.builder()
                .name("Risorsa")
                .capacity(1)
                .tenant(tenant)
                .build();
        testEntityManager.persistAndFlush(resource);

        service = Service.builder()
                .name("Servizio")
                .durationMinutes(60)
                .tenant(tenant)
                .build();
        testEntityManager.persistAndFlush(service);
    }

    @Test
    void countOverlappingBookings_ShouldReturnCount_WhenOverlapExists() {
        LocalDateTime start1 = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end1 = start1.plusHours(1); // 10:00 - 11:00

        Booking booking1 = Booking.builder()
                .tenant(tenant).resource(resource).service(service)
                .startTime(start1).endTime(end1).status(BookingStatus.CONFIRMED)
                .customerName("C1").customerEmail("c1@test.com")
                .build();
        testEntityManager.persistAndFlush(booking1);

        // Overlap: 10:30 - 11:30
        LocalDateTime start2 = start1.plusMinutes(30);
        LocalDateTime end2 = start2.plusHours(1);

        long count = bookingRepository.countOverlappingBookings(resource.getId(), start2, end2);
        
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void countOverlappingBookings_ShouldReturnZero_WhenNoOverlap() {
        LocalDateTime start1 = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end1 = start1.plusHours(1); // 10:00 - 11:00

        Booking booking1 = Booking.builder()
                .tenant(tenant).resource(resource).service(service)
                .startTime(start1).endTime(end1).status(BookingStatus.CONFIRMED)
                .customerName("C1").customerEmail("c1@test.com")
                .build();
        testEntityManager.persistAndFlush(booking1);

        // No Overlap: 11:00 - 12:00 (Adiacenti)
        LocalDateTime start2 = end1;
        LocalDateTime end2 = start2.plusHours(1);

        long count = bookingRepository.countOverlappingBookings(resource.getId(), start2, end2);
        
        assertThat(count).isEqualTo(0L); // Adiacenti non si sovrappongono
    }

    @Test
    void countOverlappingBookings_ShouldReturnZero_WhenCanceled() {
        LocalDateTime start1 = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end1 = start1.plusHours(1);

        Booking booking1 = Booking.builder()
                .tenant(tenant).resource(resource).service(service)
                .startTime(start1).endTime(end1)
                .status(BookingStatus.CANCELED) // CANCELED
                .customerName("C1").customerEmail("c1@test.com")
                .build();
        testEntityManager.persistAndFlush(booking1);

        // Overlap ma prenotazione annullata
        long count = bookingRepository.countOverlappingBookings(resource.getId(), start1, end1);
        
        assertThat(count).isEqualTo(0L);
    }
}
