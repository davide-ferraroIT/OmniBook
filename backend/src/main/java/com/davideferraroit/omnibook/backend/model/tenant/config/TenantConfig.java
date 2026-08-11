package com.davideferraroit.omnibook.backend.model.tenant.config;

import java.util.List;
import java.util.Set;

public record TenantConfig(
    String primaryColor,
    Terminology terminology,
    List<CustomField> customFormFields,
    Set<FeatureModule> activeModules,
    PaymentConfig paymentConfig,
    List<DaySchedule> businessHours,
    Boolean allowAutoAssignment,
    Boolean autoAcceptBookings
) {
}
