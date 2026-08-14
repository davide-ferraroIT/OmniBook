package com.davideferraroit.omnibook.backend.model.tenant.config;

import java.time.DayOfWeek;
import java.util.List;

public record DaySchedule(
    DayOfWeek dayOfWeek,
    boolean isOpen,
    List<TimeSlot> timeSlots
) {}
