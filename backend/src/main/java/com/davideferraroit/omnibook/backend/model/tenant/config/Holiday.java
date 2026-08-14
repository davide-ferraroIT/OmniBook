package com.davideferraroit.omnibook.backend.model.tenant.config;

import java.time.LocalDate;

public record Holiday(
    LocalDate startDate,
    LocalDate endDate,
    String description
) {}
