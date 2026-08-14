package com.davideferraroit.omnibook.backend.dto.auth;

import com.davideferraroit.omnibook.backend.model.auth.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private UUID userId;
    private Role role;
    private UUID tenantId;
}
