package com.davideferraroit.omnibook.backend.service;

import com.davideferraroit.omnibook.backend.dto.booking.BookingCreateRequest;
import com.davideferraroit.omnibook.backend.dto.booking.BookingResponse;
import com.davideferraroit.omnibook.backend.dto.resource.ResourceResponse;
import com.davideferraroit.omnibook.backend.dto.service.ServiceResponse;
import com.davideferraroit.omnibook.backend.exception.ResourceNotFoundException;
import com.davideferraroit.omnibook.backend.model.booking.Booking;
import com.davideferraroit.omnibook.backend.model.booking.BookingRepository;
import com.davideferraroit.omnibook.backend.model.booking.BookingStatus;
import com.davideferraroit.omnibook.backend.model.resource.Resource;
import com.davideferraroit.omnibook.backend.model.resource.ResourceRepository;
import com.davideferraroit.omnibook.backend.model.service.ServiceRepository;
import com.davideferraroit.omnibook.backend.model.tenant.Tenant;
import com.davideferraroit.omnibook.backend.model.tenant.TenantRepository;
import com.davideferraroit.omnibook.backend.model.tenant.config.DaySchedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TenantRepository tenantRepository;
    private final ServiceRepository serviceRepository;
    private final ResourceRepository resourceRepository;

    @Transactional(readOnly = true)
    public Page<BookingResponse> findAllByTenant(UUID tenantId, Pageable pageable) {
        return bookingRepository.findByTenantId(tenantId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public BookingResponse findByIdAndTenantId(UUID id, UUID tenantId) {
        return bookingRepository.findByIdAndTenantId(id, tenantId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Prenotazione non trovata o non appartenente al tenant."));
    }

    @Transactional(readOnly = true)
    public List<LocalTime> getAvailableSlots(UUID tenantId, UUID serviceId, UUID resourceId, LocalDate date) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant non trovato"));

        com.davideferraroit.omnibook.backend.model.service.Service service = serviceRepository.findByIdAndTenantId(serviceId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Servizio non trovato"));

        List<DaySchedule> businessHours = tenant.getConfig().businessHours();
        if (businessHours == null || businessHours.isEmpty()) {
            return List.of(); // Nessun orario configurato
        }

        Optional<DaySchedule> todaySchedule = businessHours.stream()
                .filter(schedule -> schedule.dayOfWeek() == date.getDayOfWeek() && schedule.isOpen())
                .findFirst();

        if (todaySchedule.isEmpty()) {
            return List.of(); // Chiuso in questo giorno
        }

        LocalTime openTime = todaySchedule.get().openTime();
        LocalTime closeTime = todaySchedule.get().closeTime();
        
        List<LocalTime> availableSlots = new ArrayList<>();
        int stepMinutes = service.getDurationMinutes();
        
        LocalTime currentSlot = openTime;
        while (currentSlot.plusMinutes(stepMinutes).isBefore(closeTime) || currentSlot.plusMinutes(stepMinutes).equals(closeTime)) {
            LocalDateTime start = LocalDateTime.of(date, currentSlot);
            LocalDateTime end = start.plusMinutes(stepMinutes);

            if (start.isBefore(LocalDateTime.now())) {
                currentSlot = currentSlot.plusMinutes(stepMinutes);
                continue;
            }

            boolean isSlotAvailable = false;
            
            if (resourceId != null) {
                isSlotAvailable = isResourceAvailable(resourceId, start, end);
            } else {
                // Auto-assegnazione: controlla se almeno una risorsa tra quelle del servizio è libera
                for (Resource r : service.getAllowedResources()) {
                    if (isResourceAvailable(r.getId(), start, end)) {
                        isSlotAvailable = true;
                        break;
                    }
                }
            }

            if (isSlotAvailable) {
                availableSlots.add(currentSlot);
            }
            
            currentSlot = currentSlot.plusMinutes(stepMinutes);
        }

        return availableSlots;
    }

    @Transactional(readOnly = true)
    public List<com.davideferraroit.omnibook.backend.dto.booking.DayResourceAvailability> getAvailabilityRange(
            UUID tenantId, UUID serviceId, LocalDate startDate, LocalDate endDate) {
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant non trovato"));

        com.davideferraroit.omnibook.backend.model.service.Service service = serviceRepository.findByIdAndTenantId(serviceId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Servizio non trovato"));

        List<com.davideferraroit.omnibook.backend.dto.booking.DayResourceAvailability> results = new ArrayList<>();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            final LocalDate dateToProcess = currentDate; // for lambda
            List<DaySchedule> businessHours = tenant.getConfig().businessHours();
            
            if (businessHours != null && !businessHours.isEmpty()) {
                Optional<DaySchedule> todaySchedule = businessHours.stream()
                        .filter(schedule -> schedule.dayOfWeek() == dateToProcess.getDayOfWeek() && schedule.isOpen())
                        .findFirst();

                if (todaySchedule.isPresent()) {
                    // For each allowed resource, get slots
                    for (Resource resource : service.getAllowedResources()) {
                        List<LocalTime> slots = getAvailableSlots(tenantId, serviceId, resource.getId(), dateToProcess);
                        if (!slots.isEmpty()) {
                            // Extract day label (e.g. "mar 18")
                            String dayLabel = java.time.format.DateTimeFormatter.ofPattern("E d", java.util.Locale.ITALIAN).format(dateToProcess);
                            ResourceResponse resDto = new ResourceResponse(resource.getId(), resource.getName(), resource.getCapacity(), resource.getImageUrl());
                            
                            results.add(com.davideferraroit.omnibook.backend.dto.booking.DayResourceAvailability.builder()
                                    .date(dateToProcess)
                                    .dayLabel(dayLabel)
                                    .resource(resDto)
                                    .availableSlots(slots)
                                    .build());
                        }
                    }
                }
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return results;
    }

    private boolean isResourceAvailable(UUID resourceId, LocalDateTime start, LocalDateTime end) {
        Resource r = resourceRepository.findById(resourceId).orElse(null);
        if (r == null) return false;
        long overlappingCount = bookingRepository.countOverlappingBookings(resourceId, start, end);
        return overlappingCount < r.getCapacity();
    }

    @Transactional
    public BookingResponse create(UUID tenantId, BookingCreateRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant non trovato"));

        com.davideferraroit.omnibook.backend.model.service.Service service = serviceRepository.findByIdAndTenantId(request.serviceId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Servizio non trovato"));

        LocalDateTime startTime = request.startTime();
        LocalDateTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        Resource assignedResource = null;

        if (request.resourceId() != null) {
            Resource r = resourceRepository.findByIdAndTenantId(request.resourceId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Risorsa non trovata o non appartenente al tenant."));
            boolean isAllowed = service.getAllowedResources().stream()
                    .anyMatch(res -> res.getId().equals(r.getId()));
            if (!isAllowed) {
                throw new IllegalArgumentException("La risorsa richiesta non eroga questo servizio.");
            }
            if (!isResourceAvailable(r.getId(), startTime, endTime)) {
                throw new IllegalStateException("La risorsa non è disponibile per l'orario richiesto.");
            }
            assignedResource = r;
        } else {
            Boolean allowAuto = tenant.getConfig().allowAutoAssignment();
            if (allowAuto == null || !allowAuto) {
                throw new IllegalArgumentException("Il negozio non permette l'auto-assegnazione della risorsa. Devi specificare una risorsa.");
            }
            for (Resource r : service.getAllowedResources()) {
                if (isResourceAvailable(r.getId(), startTime, endTime)) {
                    assignedResource = r;
                    break;
                }
            }
            if (assignedResource == null) {
                throw new IllegalStateException("Nessuna risorsa disponibile per l'orario richiesto.");
            }
        }

        Boolean autoAccept = tenant.getConfig().autoAcceptBookings();
        BookingStatus initialStatus = (autoAccept != null && autoAccept) ? BookingStatus.CONFIRMED : BookingStatus.PENDING;

        Booking booking = Booking.builder()
                .tenant(tenant)
                .service(service)
                .resource(assignedResource)
                .startTime(startTime)
                .endTime(endTime)
                .status(initialStatus)
                .customerName(request.customerName())
                .customerEmail(request.customerEmail())
                .customerPhone(request.customerPhone())
                .build();

        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse updateStatus(UUID id, UUID tenantId, BookingStatus newStatus) {
        Booking booking = bookingRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Prenotazione non trovata."));
        booking.setStatus(newStatus);
        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse updateBooking(UUID id, UUID tenantId, com.davideferraroit.omnibook.backend.dto.booking.BookingUpdateRequest request) {
        Booking booking = bookingRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Prenotazione non trovata."));

        com.davideferraroit.omnibook.backend.model.service.Service service = serviceRepository.findByIdAndTenantId(request.serviceId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Servizio non trovato"));

        LocalDateTime startTime = request.startTime();
        LocalDateTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        Resource assignedResource = null;
        if (request.resourceId() != null) {
            assignedResource = resourceRepository.findByIdAndTenantId(request.resourceId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Risorsa non trovata."));
        } else if (booking.getResource() != null) {
            assignedResource = booking.getResource();
        }

        // Se orario o risorsa cambiata, andrebbe riverificata la disponibilità, per semplicità qui aggiorniamo
        booking.setService(service);
        if (assignedResource != null) booking.setResource(assignedResource);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setCustomerName(request.customerName());
        booking.setCustomerEmail(request.customerEmail());
        booking.setCustomerPhone(request.customerPhone());

        return mapToResponse(bookingRepository.save(booking));
    }

    private BookingResponse mapToResponse(Booking booking) {
        ResourceResponse resourceDto = new ResourceResponse(
                booking.getResource().getId(),
                booking.getResource().getName(),
                booking.getResource().getCapacity(),
                booking.getResource().getImageUrl()
        );

        ServiceResponse serviceDto = new ServiceResponse(
                booking.getService().getId(),
                booking.getService().getName(),
                booking.getService().getDurationMinutes(),
                booking.getService().getAllowedResources().stream()
                        .map(r -> new ResourceResponse(r.getId(), r.getName(), r.getCapacity(), r.getImageUrl()))
                        .collect(Collectors.toSet()),
                booking.getService().getImageUrl()
        );

        return new BookingResponse(
                booking.getId(),
                serviceDto,
                resourceDto,
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus(),
                booking.getCustomerName(),
                booking.getCustomerEmail(),
                booking.getCustomerPhone()
        );
    }
}
