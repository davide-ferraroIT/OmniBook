package com.davideferraroit.omnibook.backend.dto.resource;

import java.util.UUID;

public record ResourceResponse(
    UUID id,
    String name,
    int capacity,
    String imageUrl
) {}
