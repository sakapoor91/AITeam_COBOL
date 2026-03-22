package com.scalefirstai.evolution.carddemo.transactions.controller;

import com.scalefirstai.evolution.carddemo.transactions.dto.TransactionDto;
import com.scalefirstai.evolution.carddemo.transactions.dto.TransactionListResponse;
import com.scalefirstai.evolution.carddemo.transactions.dto.TransactionRequest;
import com.scalefirstai.evolution.carddemo.transactions.service.TransactionService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST controller for transaction operations.
 * <p>
 * Modernized from COBOL programs COTRN00C.CBL (transaction list)
 * and COTRN01C.CBL / COTRN02C.CBL (transaction entry/processing).
 * </p>
 */
@Path("/api/v1/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionController {

    @Inject
    TransactionService transactionService;

    /**
     * Lists transactions for a given card number with pagination.
     * <p>
     * COBOL source: COTRN00C.CBL - PROCESS-PAGE-FORWARD paragraph.
     * Original browses TRANSACT VSAM file by card number.
     * </p>
     *
     * @param cardNumber the card number to filter transactions
     * @param page       the page number (0-based)
     * @param size       the page size
     * @return paginated list of transactions
     */
    @GET
    public Response listTransactions(
            @QueryParam("cardNumber") String cardNumber,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        TransactionListResponse response = transactionService.listByCardNumber(cardNumber, page, size);
        return Response.ok(response).build();
    }

    /**
     * Creates a new transaction.
     * <p>
     * COBOL source: COTRN02C.CBL - PROCESS-TRANSACTION paragraph.
     * Original validates card, checks limits, writes to TRANSACT VSAM,
     * and updates CARDDAT and ACCTDAT balances.
     * </p>
     *
     * @param request the transaction request data
     * @return the created transaction details
     */
    @POST
    public Response createTransaction(TransactionRequest request) {
        try {
            TransactionDto created = transactionService.createTransaction(request);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of("error", e.getMessage()))
                    .build();
        }
    }
}
