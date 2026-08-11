package com.davideferraroit.omnibook.backend.controller;

import com.davideferraroit.omnibook.backend.dto.booking.BookingCreateRequest;
import com.davideferraroit.omnibook.backend.dto.booking.BookingResponse;
import com.davideferraroit.omnibook.backend.model.booking.BookingStatus;
import com.davideferraroit.omnibook.backend.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @PathVariable UUID tenantId,
            @Valid @RequestBody BookingCreateRequest request) {
        log.info("Richiesta creazione prenotazione per tenant: {}", tenantId);
        BookingResponse response = bookingService.create(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<BookingResponse>> getAllBookings(
            @PathVariable UUID tenantId,
            @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
        log.info("Richiesta paginazione prenotazioni per tenant: {}", tenantId);
        return ResponseEntity.ok(bookingService.findAllByTenant(tenantId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        log.info("Richiesta recupero prenotazione ID: {} per tenant: {}", id, tenantId);
        return ResponseEntity.ok(bookingService.findByIdAndTenantId(id, tenantId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BookingResponse> updateStatus(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @RequestParam BookingStatus status) {
        log.info("Richiesta cambio stato a {} per prenotazione ID: {} (tenant: {})", status, id, tenantId);
        return ResponseEntity.ok(bookingService.updateStatus(id, tenantId, status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody com.davideferraroit.omnibook.backend.dto.booking.BookingUpdateRequest request) {
        log.info("Richiesta modifica per prenotazione ID: {} (tenant: {})", id, tenantId);
        return ResponseEntity.ok(bookingService.updateBooking(id, tenantId, request));
    }

    @GetMapping("/availability")
    public ResponseEntity<List<LocalTime>> getAvailability(
            @PathVariable UUID tenantId,
            @RequestParam UUID serviceId,
            @RequestParam(required = false) UUID resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Richiesta disponibilità tenant: {}, servizio: {}, data: {}", tenantId, serviceId, date);
        List<LocalTime> availableSlots = bookingService.getAvailableSlots(tenantId, serviceId, resourceId, date);
        return ResponseEntity.ok(availableSlots);
    }
}
