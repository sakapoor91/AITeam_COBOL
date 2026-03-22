package com.scalefirstai.evolution.carddemo.auth.service;

import com.scalefirstai.evolution.carddemo.auth.dto.LoginRequest;
import com.scalefirstai.evolution.carddemo.auth.dto.LoginResponse;
import com.scalefirstai.evolution.carddemo.auth.exception.AuthenticationException;
import com.scalefirstai.evolution.carddemo.auth.model.UserSecurity;
import com.scalefirstai.evolution.carddemo.auth.repository.UserSecurityRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

/**
 * Service for user authentication and credential validation.
 * <p>
 * Modernized from COBOL program COSGN00C.CBL.
 * Original logic reads USRSEC VSAM file keyed by SEC-USR-ID,
 * compares SEC-USR-PWD, and returns SEC-USR-TYPE and name fields.
 * </p>
 */
@ApplicationScoped
public class AuthService {

    @Inject
    UserSecurityRepository userSecurityRepository;

    /**
     * Validates user credentials and returns a login response.
     * <p>
     * COBOL source: COSGN00C.CBL - PROCESS-ENTER-KEY paragraph.
     * Reads USRSEC file by SEC-USR-ID, validates SEC-USR-PWD.
     * </p>
     *
     * @param request the login request containing userId and password
     * @return LoginResponse with authenticated user details
     * @throws AuthenticationException if credentials are invalid
     */
    public LoginResponse authenticate(LoginRequest request) {
        if (request.userId() == null || request.userId().isBlank()) {
            throw new AuthenticationException("User ID is required");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new AuthenticationException("Password is required");
        }

        UserSecurity user = userSecurityRepository.findByUserId(request.userId());
        if (user == null) {
            throw new AuthenticationException("Invalid user ID or password");
        }

        if (!user.getPassword().equals(request.password())) {
            throw new AuthenticationException("Invalid user ID or password");
        }

        String token = UUID.randomUUID().toString();

        return new LoginResponse(
                user.getUserId(),
                user.getUserType(),
                user.getFirstName(),
                user.getLastName(),
                token
        );
    }
}
