package com.davideferraroit.omnibook.backend.dto.booking;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingCreateRequest(
    @NotNull(message = "Il servizio è obbligatorio")
    UUID serviceId,

    UUID resourceId, // Opzionale se il tenant ha allowAutoAssignment=true

    @NotNull(message = "L'orario di inizio è obbligatorio")
    @Future(message = "La prenotazione deve essere nel futuro")
    LocalDateTime startTime,

    @NotBlank(message = "Il nome del cliente è obbligatorio")
    String customerName,

    @NotBlank(message = "L'email del cliente è obbligatoria")
    @Email(message = "Formato email non valido")
    String customerEmail,

    String customerPhone
) {}
