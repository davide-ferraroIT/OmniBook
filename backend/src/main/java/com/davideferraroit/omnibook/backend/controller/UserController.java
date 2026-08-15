package com.davideferraroit.omnibook.backend.controller;

import com.davideferraroit.omnibook.backend.dto.auth.UserProfileResponse;
import com.davideferraroit.omnibook.backend.model.auth.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final com.davideferraroit.omnibook.backend.model.auth.UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        log.info("Ricevuta richiesta profilo per utente: {}", user.getEmail());
        
        UserProfileResponse response = UserProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .tenantName(user.getTenant() != null ? user.getTenant().getName() : null)
                .tenantSlug(user.getTenant() != null ? user.getTenant().getSlug() : null)
                .build();
                
        return ResponseEntity.ok(response);
    }

    @org.springframework.web.bind.annotation.PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateCurrentUser(@AuthenticationPrincipal User user, @org.springframework.web.bind.annotation.RequestBody com.davideferraroit.omnibook.backend.dto.auth.UserProfileUpdateRequest request) {
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        
        userRepository.save(user);

        UserProfileResponse response = UserProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .tenantName(user.getTenant() != null ? user.getTenant().getName() : null)
                .tenantSlug(user.getTenant() != null ? user.getTenant().getSlug() : null)
                .build();

        return ResponseEntity.ok(response);
    }
}

