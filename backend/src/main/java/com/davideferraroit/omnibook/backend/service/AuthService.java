package com.davideferraroit.omnibook.backend.service;

import com.davideferraroit.omnibook.backend.dto.auth.AuthenticationRequest;
import com.davideferraroit.omnibook.backend.dto.auth.AuthenticationResponse;
import com.davideferraroit.omnibook.backend.dto.auth.CustomerRegisterRequest;
import com.davideferraroit.omnibook.backend.dto.auth.TenantRegisterRequest;
import com.davideferraroit.omnibook.backend.dto.tenant.TenantResponse;
import com.davideferraroit.omnibook.backend.model.auth.Role;
import com.davideferraroit.omnibook.backend.model.auth.User;
import com.davideferraroit.omnibook.backend.model.auth.UserRepository;
import com.davideferraroit.omnibook.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TenantService tenantService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthenticationResponse registerCustomer(CustomerRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email già in uso");
        }

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .tenantId(null) // Global customer
                .build();
        
        userRepository.save(user);
        
        var jwtToken = generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Transactional
    public AuthenticationResponse registerTenant(TenantRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email già in uso");
        }

        // First, create the Tenant
        TenantResponse tenant = tenantService.create(request.getTenantDetails());

        // Then, create the User for this tenant
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.TENANT_ADMIN)
                .tenantId(tenant.id())
                .build();

        userRepository.save(user);

        var jwtToken = generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
                
        var jwtToken = generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
    
    private String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        if (user.getTenantId() != null) {
            extraClaims.put("tenantId", user.getTenantId().toString());
        }
        return jwtService.generateToken(extraClaims, user);
    }
}
