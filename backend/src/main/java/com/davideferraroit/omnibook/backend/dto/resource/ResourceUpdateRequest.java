package com.davideferraroit.omnibook.backend.dto.resource;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ResourceUpdateRequest(
    @NotBlank(message = "Il nome della risorsa è obbligatorio")
    String name,

    @Min(value = 1, message = "La capacità minima è 1")
    int capacity,

    String imageUrl
) {}
