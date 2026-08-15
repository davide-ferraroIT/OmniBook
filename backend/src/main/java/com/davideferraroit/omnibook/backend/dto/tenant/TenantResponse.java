package com.davideferraroit.omnibook.backend.dto.tenant;

import com.davideferraroit.omnibook.backend.model.tenant.config.TenantConfig;

import java.util.UUID;

public record TenantResponse(
    UUID id,
    String name,
    String slug,
    String inviteCode,
    TenantConfig config
) {}
