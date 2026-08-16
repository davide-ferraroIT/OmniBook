package com.davideferraroit.omnibook.backend.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set the secret key via reflection (simulating @Value injection)
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1000 * 60 * 60 * 24L); // 1 day

        userDetails = User.builder()
                .username("test@omnibook.it")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String token = jwtService.generateToken(userDetails);
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void extractUsername_ShouldReturnCorrectSubject() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("test@omnibook.it");
    }

    @Test
    void isTokenValid_ShouldReturnTrueForValidToken() {
        String token = jwtService.generateToken(userDetails);
        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_ShouldReturnFalseForDifferentUser() {
        String token = jwtService.generateToken(userDetails);
        
        UserDetails differentUser = User.builder()
                .username("other@omnibook.it")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
                
        boolean isValid = jwtService.isTokenValid(token, differentUser);
        assertThat(isValid).isFalse();
    }

    @Test
    void tokenWithExtraClaims_ShouldContainClaims() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("tenantId", "12345");

        String token = jwtService.generateToken(extraClaims, userDetails);
        
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        String tenantId = jwtService.extractClaim(token, claims -> claims.get("tenantId", String.class));
        
        assertThat(role).isEqualTo("ADMIN");
        assertThat(tenantId).isEqualTo("12345");
    }

    @Test
    void extractUsername_ShouldThrowExceptionForTamperedToken() {
        String token = jwtService.generateToken(userDetails);
        String tamperedToken = token + "bad";

        assertThatThrownBy(() -> jwtService.extractUsername(tamperedToken))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void isTokenExpired_ShouldThrowExceptionForExpiredToken() {
        // Set a negative expiration time to create an expired token
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);
        String token = jwtService.generateToken(userDetails);

        assertThatThrownBy(() -> jwtService.isTokenValid(token, userDetails))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
