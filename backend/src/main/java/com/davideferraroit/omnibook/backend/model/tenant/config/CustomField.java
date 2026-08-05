package com.davideferraroit.omnibook.backend.model.tenant.config;

public record CustomField(
    String name,
    String label,
    String type, // e.g. "text", "number", "date", "select"
    boolean required
) {
}
