package com.davideferraroit.omnibook.backend.model.tenant.config;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record DaySchedule(
    DayOfWeek dayOfWeek,
    boolean isOpen,
    LocalTime openTime,
    LocalTime closeTime
) {}
