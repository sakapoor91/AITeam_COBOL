package com.scalefirstai.evolution.carddemo.accounts.controller;

import com.scalefirstai.evolution.carddemo.accounts.dto.AccountDto;
import com.scalefirstai.evolution.carddemo.accounts.dto.AccountUpdateRequest;
import com.scalefirstai.evolution.carddemo.accounts.service.AccountService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST controller for account management operations.
 * <p>
 * Modernized from COBOL programs COACTUPC.CBL (account update)
 * and COACTVWC.CBL (account view). Original programs interact
 * with ACCTDAT VSAM file via CICS commands.
 * </p>
 */
@Path("/api/v1/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountController {

    @Inject
    AccountService accountService;

    /**
     * Retrieves an account by its account ID.
     * <p>
     * COBOL source: COACTVWC.CBL - READ-ACCT-DATA paragraph.
     * Original reads ACCTDAT VSAM file by ACCT-ID key.
     * </p>
     *
     * @param accountId the account identifier
     * @return the account details
     */
    @GET
    @Path("/{accountId}")
    public Response getAccount(@PathParam("accountId") String accountId) {
        AccountDto account = accountService.getByAccountId(accountId);
        if (account == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of("error", "Account not found"))
                    .build();
        }
        return Response.ok(account).build();
    }

    /**
     * Updates an account's details.
     * <p>
     * COBOL source: COACTUPC.CBL - UPDATE-ACCT-DATA paragraph.
     * Original rewrites ACCTDAT VSAM record after validation.
     * </p>
     *
     * @param accountId the account identifier to update
     * @param request   the update request data
     * @return the updated account details
     */
    @PUT
    @Path("/{accountId}")
    public Response updateAccount(
            @PathParam("accountId") String accountId,
            AccountUpdateRequest request) {
        AccountDto updated = accountService.updateAccount(accountId, request);
        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of("error", "Account not found"))
                    .build();
        }
        return Response.ok(updated).build();
    }
}
