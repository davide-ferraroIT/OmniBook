package com.davideferraroit.omnibook.backend.dto.tenant;

import com.davideferraroit.omnibook.backend.model.tenant.config.TenantConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record TenantCreateRequest(
    @NotBlank(message = "Il nome del tenant è obbligatorio")
    String name,

    @NotBlank(message = "Lo slug è obbligatorio")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Lo slug può contenere solo lettere minuscole, numeri e trattini")
    String slug,

    @NotNull(message = "La configurazione (config) non può essere nulla")
    TenantConfig config
) {}
