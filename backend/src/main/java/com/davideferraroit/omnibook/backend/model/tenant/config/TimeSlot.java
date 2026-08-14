package com.davideferraroit.omnibook.backend.model.tenant.config;

import java.time.LocalTime;

public record TimeSlot(
    LocalTime startTime,
    LocalTime endTime
) {}
