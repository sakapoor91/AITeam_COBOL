package com.scalefirstai.evolution.carddemo.auth.repository;

import com.scalefirstai.evolution.carddemo.auth.model.UserSecurity;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository for UserSecurity entity persistence operations.
 * <p>
 * Modernized from COBOL VSAM READ operations on USRSEC file
 * in program COSGN00C.CBL. Original keyed access by SEC-USR-ID.
 * </p>
 */
@ApplicationScoped
public class UserSecurityRepository implements PanacheRepositoryBase<UserSecurity, String> {

    /**
     * Finds a user security record by user ID.
     * <p>
     * COBOL source: COSGN00C.CBL - READ USRSEC-FILE KEY IS SEC-USR-ID.
     * </p>
     *
     * @param userId the user identifier to search for
     * @return the UserSecurity record, or null if not found
     */
    public UserSecurity findByUserId(String userId) {
        return findById(userId);
    }
}
