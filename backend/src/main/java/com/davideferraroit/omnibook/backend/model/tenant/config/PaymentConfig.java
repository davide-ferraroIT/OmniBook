package com.davideferraroit.omnibook.backend.model.tenant.config;

public record PaymentConfig(
    String stripePublicKey,
    String currency // e.g. "EUR", "USD"
) {
}
