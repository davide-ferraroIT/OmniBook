package com.davideferraroit.omnibook.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SecurityConfigTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpoints_ShouldBeAccessibleWithoutAuth() throws Exception {
        // Swagger UI
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());

        // Auth endpoint (no body needed for 400 Bad Request, just ensuring it's not 401/403)
        mockMvc.perform(get("/api/v1/auth/some-path-doesnt-exist"))
                .andExpect(status().isNotFound());
    }

    @Test
    void protectedEndpoints_ShouldReturn403_WhenNotAuthenticated() throws Exception {
        // Unauthenticated access to protected resource
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isForbidden()); // Spring Security default without custom entry point is 403
    }

    @Test
    void protectedEndpoints_ShouldReturn200_WhenAuthenticated() throws Exception {
        // Create a custom user to avoid ClassCastException in UserController which expects the custom User class
        com.davideferraroit.omnibook.backend.model.auth.User customUser = com.davideferraroit.omnibook.backend.model.auth.User.builder()
                .id(java.util.UUID.randomUUID())
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password")
                .role(com.davideferraroit.omnibook.backend.model.auth.Role.CUSTOMER)
                .build();

        mockMvc.perform(get("/api/v1/users/me")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(customUser)))
                .andExpect(status().isOk());
    }

    @Test
    void corsOptionsRequest_ShouldReturnOk_WithAllowedOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
