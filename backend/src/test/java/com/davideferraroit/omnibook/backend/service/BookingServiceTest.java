package com.davideferraroit.omnibook.backend.service;

import org.junit.jupiter.api.DisplayName;
import com.davideferraroit.omnibook.backend.dto.booking.BookingCreateRequest;
import com.davideferraroit.omnibook.backend.dto.booking.BookingResponse;
import com.davideferraroit.omnibook.backend.model.booking.Booking;
import com.davideferraroit.omnibook.backend.model.booking.BookingRepository;
import com.davideferraroit.omnibook.backend.model.booking.BookingStatus;
import com.davideferraroit.omnibook.backend.model.resource.Resource;
import com.davideferraroit.omnibook.backend.model.resource.ResourceRepository;
import com.davideferraroit.omnibook.backend.model.service.Service;
import com.davideferraroit.omnibook.backend.model.service.ServiceRepository;
import com.davideferraroit.omnibook.backend.model.tenant.Tenant;
import com.davideferraroit.omnibook.backend.model.tenant.TenantRepository;
import com.davideferraroit.omnibook.backend.model.tenant.config.DaySchedule;
import com.davideferraroit.omnibook.backend.model.tenant.config.Holiday;
import com.davideferraroit.omnibook.backend.model.tenant.config.TenantConfig;
import com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("Booking Service Unit Tests")
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private ResourceRepository resourceRepository;

    @InjectMocks
    private BookingService bookingService;

    private Tenant tenant;
    private Service service;
    private Resource resource;
    private UUID tenantId = UUID.randomUUID();
    private UUID serviceId = UUID.randomUUID();
    private UUID resourceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantConfig config = new TenantConfig(
                "#000000",
                null,
                null,
                null,
                null,
                List.of(new DaySchedule(DayOfWeek.MONDAY, true, List.of(new TimeSlot(LocalTime.of(9, 0), LocalTime.of(18, 0))))),
                true,
                true,
                List.of(new Holiday(LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 26), "Natale"))
        );

        tenant = Tenant.builder().id(tenantId).config(config).build();
        resource = Resource.builder().id(resourceId).name("Poltrona 1").capacity(1).tenant(tenant).build();
        service = Service.builder()
                .id(serviceId)
                .name("Taglio Capelli")
                .durationMinutes(30)
                .tenant(tenant)
                .allowedResources(Set.of(resource))
                .build();
    }

    @Test
    @DisplayName("Creazione prenotazione con risorsa esplicita - Successo")
    void create_ShouldSucceed_WithExplicitResource() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        BookingCreateRequest request = new BookingCreateRequest(serviceId, resourceId, startTime, "Test", "test@test.com", "123");

        given(tenantRepository.findById(tenantId)).willReturn(Optional.of(tenant));
        given(serviceRepository.findByIdAndTenantId(serviceId, tenantId)).willReturn(Optional.of(service));
        given(resourceRepository.findByIdAndTenantId(resourceId, tenantId)).willReturn(Optional.of(resource));
        given(resourceRepository.findById(resourceId)).willReturn(Optional.of(resource));
        given(bookingRepository.countOverlappingBookings(eq(resourceId), eq(startTime), any(LocalDateTime.class))).willReturn(0L);
        given(bookingRepository.save(any(Booking.class))).willAnswer(i -> {
            Booking b = i.getArgument(0);
            b.setId(UUID.randomUUID());
            return b;
        });

        // When
        BookingResponse response = bookingService.create(tenantId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(BookingStatus.CONFIRMED); // because autoAccept is true
        then(bookingRepository).should().save(any(Booking.class));
    }

    @Test
    @DisplayName("Creazione prenotazione - Eccezione se risorsa non eroga il servizio")
    void create_ShouldThrowException_WhenResourceNotAllowedForService() {
        // Given
        Resource otherResource = Resource.builder().id(UUID.randomUUID()).name("Other").capacity(1).build();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        BookingCreateRequest request = new BookingCreateRequest(serviceId, otherResource.getId(), startTime, "Test", "t@t.com", "123");

        given(tenantRepository.findById(tenantId)).willReturn(Optional.of(tenant));
        given(serviceRepository.findByIdAndTenantId(serviceId, tenantId)).willReturn(Optional.of(service));
        given(resourceRepository.findByIdAndTenantId(otherResource.getId(), tenantId)).willReturn(Optional.of(otherResource));

        // When & Then
        assertThatThrownBy(() -> bookingService.create(tenantId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La risorsa richiesta non eroga questo servizio");
    }

    @Test
    @DisplayName("Creazione prenotazione - Eccezione se la risorsa non è disponibile (Overlap/Full)")
    void create_ShouldThrowException_WhenResourceIsFull() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        BookingCreateRequest request = new BookingCreateRequest(serviceId, resourceId, startTime, "Test", "t@t.com", "123");

        given(tenantRepository.findById(tenantId)).willReturn(Optional.of(tenant));
        given(serviceRepository.findByIdAndTenantId(serviceId, tenantId)).willReturn(Optional.of(service));
        given(resourceRepository.findByIdAndTenantId(resourceId, tenantId)).willReturn(Optional.of(resource));
        given(resourceRepository.findById(resourceId)).willReturn(Optional.of(resource));
        
        given(bookingRepository.countOverlappingBookings(eq(resourceId), any(), any())).willReturn(1L); // Risorsa piena (capacity = 1, trovate 1 sovrapposizione)

        // When & Then
        assertThatThrownBy(() -> bookingService.create(tenantId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non è disponibile");
    }

    @Test
    @DisplayName("Creazione prenotazione - Successo con Assegnazione Automatica (Risorsa Null)")
    void create_ShouldSucceed_WithAutoAssignment() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        BookingCreateRequest request = new BookingCreateRequest(serviceId, null, startTime, "Test", "t@t.com", "123");

        given(tenantRepository.findById(tenantId)).willReturn(Optional.of(tenant));
        given(serviceRepository.findByIdAndTenantId(serviceId, tenantId)).willReturn(Optional.of(service));
        given(resourceRepository.findById(resourceId)).willReturn(Optional.of(resource));
        given(bookingRepository.countOverlappingBookings(eq(resourceId), any(), any())).willReturn(0L);
        given(bookingRepository.save(any(Booking.class))).willAnswer(i -> {
            Booking b = i.getArgument(0);
            b.setId(UUID.randomUUID());
            return b;
        });

        // When
        BookingResponse response = bookingService.create(tenantId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.resource().id()).isEqualTo(resourceId); // assigned correctly
    }

    @Test
    @DisplayName("Ricerca slot - Ritorna lista vuota durante i giorni di ferie")
    void getAvailableSlots_ShouldReturnEmpty_WhenHoliday() {
        // Given
        LocalDate holiday = LocalDate.of(2026, 12, 25);
        given(tenantRepository.findById(tenantId)).willReturn(Optional.of(tenant));
        given(serviceRepository.findByIdAndTenantId(serviceId, tenantId)).willReturn(Optional.of(service));

        // When
        List<LocalTime> slots = bookingService.getAvailableSlots(tenantId, serviceId, resourceId, holiday);

        // Then
        assertThat(slots).isEmpty();
    }

    @Test
    @DisplayName("Ricerca slot - Ritorna la lista corretta durante i giorni di apertura")
    void getAvailableSlots_ShouldReturnSlots_WhenOpenAndNotFull() {
        // Given
        LocalDate mondayDate = LocalDate.of(2026, 8, 17); // 17 Agosto 2026 è un lunedì
        
        given(tenantRepository.findById(tenantId)).willReturn(Optional.of(tenant));
        given(serviceRepository.findByIdAndTenantId(serviceId, tenantId)).willReturn(Optional.of(service));
        given(resourceRepository.findById(resourceId)).willReturn(Optional.of(resource));
        
        given(bookingRepository.countOverlappingBookings(eq(resourceId), any(LocalDateTime.class), any(LocalDateTime.class))).willReturn(0L);

        // When
        List<LocalTime> slots = bookingService.getAvailableSlots(tenantId, serviceId, resourceId, mondayDate);

        // Then
        // Dalle 9 alle 18, slot di 30 minuti = 18 slot
        assertThat(slots).isNotEmpty();
        assertThat(slots.get(0)).isEqualTo(LocalTime.of(9, 0));
        assertThat(slots.get(1)).isEqualTo(LocalTime.of(9, 30));
    }
}
