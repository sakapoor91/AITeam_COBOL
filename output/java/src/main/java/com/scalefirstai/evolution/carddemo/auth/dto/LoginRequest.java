package com.scalefirstai.evolution.carddemo.auth.dto;

/**
 * Request DTO for user authentication.
 * <p>
 * Maps to COBOL copybook COSGN00.CPY fields:
 * SEC-USR-ID (PIC X(8)) and SEC-USR-PWD (PIC X(8)).
 * </p>
 *
 * @param userId   the user identifier (maps to SEC-USR-ID)
 * @param password the user password (maps to SEC-USR-PWD)
 */
public record LoginRequest(
        String userId,
        String password
) {
}
