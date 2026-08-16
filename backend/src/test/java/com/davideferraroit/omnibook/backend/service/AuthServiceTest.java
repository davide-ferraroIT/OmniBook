package com.davideferraroit.omnibook.backend.service;

import com.davideferraroit.omnibook.backend.dto.auth.AuthenticationRequest;
import com.davideferraroit.omnibook.backend.dto.auth.AuthenticationResponse;
import com.davideferraroit.omnibook.backend.dto.auth.CustomerRegisterRequest;
import com.davideferraroit.omnibook.backend.dto.auth.TenantRegisterRequest;
import com.davideferraroit.omnibook.backend.dto.tenant.TenantCreateRequest;
import com.davideferraroit.omnibook.backend.dto.tenant.TenantResponse;
import com.davideferraroit.omnibook.backend.exception.ResourceNotFoundException;
import com.davideferraroit.omnibook.backend.model.auth.Role;
import com.davideferraroit.omnibook.backend.model.auth.User;
import com.davideferraroit.omnibook.backend.model.auth.UserRepository;
import com.davideferraroit.omnibook.backend.model.tenant.Tenant;
import com.davideferraroit.omnibook.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantService tenantService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private Tenant tenant;
    private User user;

    @BeforeEach
    void setUp() {
        tenant = Tenant.builder()
                .id(UUID.randomUUID())
                .name("Test Tenant")
                .slug("test-tenant")
                .build();

        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@omnibook.it")
                .password("encoded_password")
                .role(Role.CUSTOMER)
                .tenant(tenant)
                .build();
    }

    @Test
    void registerCustomer_ShouldSucceed() {
        CustomerRegisterRequest request = new CustomerRegisterRequest();
        request.setEmail("new@omnibook.it");
        request.setPassword("password");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setInviteCode("INVITE123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(tenantService.findEntityByInviteCode("INVITE123")).thenReturn(tenant);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(jwtService.generateToken(any(), any(User.class))).thenReturn("jwt.token.here");

        AuthenticationResponse response = authService.registerCustomer(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt.token.here");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerCustomer_ShouldThrowException_WhenEmailExists() {
        CustomerRegisterRequest request = new CustomerRegisterRequest();
        request.setEmail("existing@omnibook.it");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.registerCustomer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email già in uso");
                
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerCustomer_ShouldThrowException_WhenInviteCodeInvalid() {
        CustomerRegisterRequest request = new CustomerRegisterRequest();
        request.setEmail("new@omnibook.it");
        request.setInviteCode("INVALID");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(tenantService.findEntityByInviteCode("INVALID"))
                .thenThrow(new ResourceNotFoundException("Codice invito non valido"));

        assertThatThrownBy(() -> authService.registerCustomer(request))
                .isInstanceOf(ResourceNotFoundException.class);
                
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerTenant_ShouldSucceed() {
        TenantRegisterRequest request = new TenantRegisterRequest();
        request.setEmail("owner@omnibook.it");
        request.setPassword("password");
        request.setFirstName("Admin");
        request.setLastName("Admin");
        TenantCreateRequest tenantDetails = new TenantCreateRequest("My Shop", "my-shop", null);
        request.setTenantDetails(tenantDetails);

        TenantResponse tenantResponse = new TenantResponse(tenant.getId(), "My Shop", "my-shop", "CODE", null);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(tenantService.create(tenantDetails)).thenReturn(tenantResponse);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(jwtService.generateToken(any(), any(User.class))).thenReturn("jwt.token");

        AuthenticationResponse response = authService.registerTenant(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt.token");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void authenticate_ShouldSucceed_WhenCredentialsValid() {
        AuthenticationRequest request = new AuthenticationRequest("test@omnibook.it", "password");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(), any(User.class))).thenReturn("valid.jwt");

        AuthenticationResponse response = authService.authenticate(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("valid.jwt");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void authenticate_ShouldThrowException_WhenCredentialsInvalid() {
        AuthenticationRequest request = new AuthenticationRequest("test@omnibook.it", "wrong");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
