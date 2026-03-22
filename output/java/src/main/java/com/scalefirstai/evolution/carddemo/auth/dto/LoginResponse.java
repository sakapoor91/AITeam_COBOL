package com.scalefirstai.evolution.carddemo.auth.dto;

/**
 * Response DTO for successful authentication.
 * <p>
 * Maps to COBOL copybook COSGN00.CPY output fields:
 * SEC-USR-ID, SEC-USR-TYPE, SEC-USR-FNAME, SEC-USR-LNAME.
 * Token is a modernization addition for stateless auth.
 * </p>
 *
 * @param userId    the authenticated user ID (maps to SEC-USR-ID)
 * @param userType  the user type/role (maps to SEC-USR-TYPE)
 * @param firstName the user's first name (maps to SEC-USR-FNAME)
 * @param lastName  the user's last name (maps to SEC-USR-LNAME)
 * @param token     the authentication token (modernization addition)
 */
public record LoginResponse(
        String userId,
        String userType,
        String firstName,
        String lastName,
        String token
) {
}
