package com.davideferraroit.omnibook.backend.controller;

import com.davideferraroit.omnibook.backend.dto.auth.AuthenticationRequest;
import com.davideferraroit.omnibook.backend.dto.auth.AuthenticationResponse;
import com.davideferraroit.omnibook.backend.dto.auth.CustomerRegisterRequest;
import com.davideferraroit.omnibook.backend.dto.auth.TenantRegisterRequest;
import com.davideferraroit.omnibook.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register-customer")
    public ResponseEntity<AuthenticationResponse> registerCustomer(
            @Valid @RequestBody CustomerRegisterRequest request
    ) {
        log.info("Ricevuta richiesta di registrazione cliente: {}", request.getEmail());
        return ResponseEntity.ok(authService.registerCustomer(request));
    }

    @PostMapping("/register-tenant")
    public ResponseEntity<AuthenticationResponse> registerTenant(
            @Valid @RequestBody TenantRegisterRequest request
    ) {
        log.info("Ricevuta richiesta di registrazione tenant: {}", request.getEmail());
        return ResponseEntity.ok(authService.registerTenant(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        log.info("Ricevuta richiesta di login per: {}", request.getEmail());
        return ResponseEntity.ok(authService.authenticate(request));
    }
}
