package com.davideferraroit.omnibook.backend.dto.service;

import com.davideferraroit.omnibook.backend.dto.resource.ResourceResponse;
import java.util.Set;
import java.util.UUID;

public record ServiceResponse(
    UUID id,
    String name,
    int durationMinutes,
    Set<ResourceResponse> allowedResources,
    String imageUrl
) {}
