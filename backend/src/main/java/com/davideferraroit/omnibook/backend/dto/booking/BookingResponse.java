package com.davideferraroit.omnibook.backend.dto.booking;

import com.davideferraroit.omnibook.backend.dto.resource.ResourceResponse;
import com.davideferraroit.omnibook.backend.dto.service.ServiceResponse;
import com.davideferraroit.omnibook.backend.model.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponse(
    UUID id,
    ServiceResponse service,
    ResourceResponse resource,
    LocalDateTime startTime,
    LocalDateTime endTime,
    BookingStatus status,
    String customerName,
    String customerEmail,
    String customerPhone
) {}
