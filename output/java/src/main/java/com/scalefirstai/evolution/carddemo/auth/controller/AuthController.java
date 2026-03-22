package com.scalefirstai.evolution.carddemo.auth.controller;

import com.scalefirstai.evolution.carddemo.auth.dto.LoginRequest;
import com.scalefirstai.evolution.carddemo.auth.dto.LoginResponse;
import com.scalefirstai.evolution.carddemo.auth.exception.AuthenticationException;
import com.scalefirstai.evolution.carddemo.auth.service.AuthService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST controller for authentication operations.
 * <p>
 * Modernized from COBOL program COSGN00C.CBL which handles
 * user sign-on via CICS SEND/RECEIVE MAP operations.
 * </p>
 */
@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    AuthService authService;

    /**
     * Authenticates a user with userId and password.
     * <p>
     * COBOL source: COSGN00C.CBL - PROCESS-ENTER-KEY paragraph.
     * Validates credentials against USRSEC (User Security) VSAM file.
     * </p>
     *
     * @param request the login credentials
     * @return LoginResponse with user details and token on success
     */
    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        try {
            LoginResponse loginResponse = authService.authenticate(request);
            return Response.ok(loginResponse).build();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(java.util.Map.of("error", e.getMessage()))
                    .build();
        }
    }
}
