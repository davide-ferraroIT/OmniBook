package com.davideferraroit.omnibook.backend.dto.booking;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingUpdateRequest(
        @NotNull(message = "L'ID del servizio è obbligatorio")
        UUID serviceId,

        UUID resourceId,

        @NotNull(message = "La data/ora di inizio è obbligatoria")
        @Future(message = "La prenotazione deve essere nel futuro")
        LocalDateTime startTime,

        @NotBlank(message = "Il nome del cliente è obbligatorio")
        String customerName,

        @NotBlank(message = "L'email del cliente è obbligatoria")
        @Email(message = "Formato email non valido")
        String customerEmail,

        String customerPhone
) {
}
