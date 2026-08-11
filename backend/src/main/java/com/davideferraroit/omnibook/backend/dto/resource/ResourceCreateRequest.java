package com.davideferraroit.omnibook.backend.dto.resource;

import com.davideferraroit.omnibook.backend.model.resource.ResourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResourceCreateRequest(
    @NotBlank(message = "Il nome della risorsa è obbligatorio")
    String name,

    @NotNull(message = "Il tipo di risorsa è obbligatorio")
    ResourceType type,

    @Min(value = 1, message = "La capacità minima è 1")
    int capacity
) {}
