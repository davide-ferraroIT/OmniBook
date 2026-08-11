package com.davideferraroit.omnibook.backend.dto.service;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record ServiceCreateRequest(
    @NotBlank(message = "Il nome del servizio è obbligatorio")
    String name,

    @Min(value = 1, message = "La durata minima è di 1 minuto")
    int durationMinutes,

    @NotNull(message = "La lista delle risorse compatibili non può essere nulla")
    Set<UUID> allowedResourceIds
) {}
