package com.scalefirstai.evolution.carddemo.auth.exception;

/**
 * Exception thrown when user authentication fails.
 * <p>
 * Modernized from COBOL program COSGN00C.CBL error handling.
 * Original sends error message to CICS terminal via SEND MAP.
 * </p>
 */
public class AuthenticationException extends RuntimeException {

    /**
     * Creates a new AuthenticationException with the specified message.
     *
     * @param message the error detail message
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Creates a new AuthenticationException with message and cause.
     *
     * @param message the error detail message
     * @param cause   the underlying cause
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
