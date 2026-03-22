package com.scalefirstai.evolution.carddemo.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity representing user security credentials.
 * <p>
 * Modernized from COBOL VSAM file USRSEC with record layout
 * defined in copybook COUSR00.CPY:
 * SEC-USR-ID (PIC X(8)), SEC-USR-PWD (PIC X(8)),
 * SEC-USR-TYPE (PIC X(1)), SEC-USR-FNAME (PIC X(20)),
 * SEC-USR-LNAME (PIC X(20)).
 * </p>
 */
@Entity
@Table(name = "user_security")
public class UserSecurity {

    @Id
    @Column(name = "user_id", length = 8, nullable = false)
    private String userId;

    @Column(name = "password", length = 8, nullable = false)
    private String password;

    @Column(name = "user_type", length = 1, nullable = false)
    private String userType;

    @Column(name = "first_name", length = 20)
    private String firstName;

    @Column(name = "last_name", length = 20)
    private String lastName;

    /** Default constructor required by JPA. */
    public UserSecurity() {
    }

    /**
     * Returns the user identifier.
     * @return the user ID (maps to SEC-USR-ID)
     */
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the user password.
     * @return the password (maps to SEC-USR-PWD)
     */
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the user type/role.
     * @return the user type (maps to SEC-USR-TYPE: 'A' = Admin, 'U' = User)
     */
    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    /**
     * Returns the user's first name.
     * @return the first name (maps to SEC-USR-FNAME)
     */
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the user's last name.
     * @return the last name (maps to SEC-USR-LNAME)
     */
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
