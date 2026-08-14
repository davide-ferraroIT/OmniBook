package com.davideferraroit.omnibook.backend.dto.booking;

import com.davideferraroit.omnibook.backend.dto.resource.ResourceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayResourceAvailability {
    private LocalDate date;
    private String dayLabel; // e.g. "mar 18"
    private ResourceResponse resource;
    private List<LocalTime> availableSlots;
}
