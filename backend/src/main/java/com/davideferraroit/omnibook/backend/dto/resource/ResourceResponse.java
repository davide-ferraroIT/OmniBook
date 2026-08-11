package com.davideferraroit.omnibook.backend.dto.resource;

import com.davideferraroit.omnibook.backend.model.resource.ResourceType;
import java.util.UUID;

public record ResourceResponse(
    UUID id,
    String name,
    ResourceType type,
    int capacity
) {}
