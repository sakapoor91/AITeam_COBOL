package com.scalefirstai.evolution.carddemo.cards.controller;

import com.scalefirstai.evolution.carddemo.cards.dto.CardListResponse;
import com.scalefirstai.evolution.carddemo.cards.dto.CardUpdateRequest;
import com.scalefirstai.evolution.carddemo.cards.dto.CreditCardDto;
import com.scalefirstai.evolution.carddemo.cards.service.CardService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST controller for credit card management operations.
 * <p>
 * Modernized from COBOL programs COCRDLIC.CBL (card list)
 * and COCRDUPC.CBL (card detail/update).
 * </p>
 */
@Path("/api/v1/cards")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CardController {

    @Inject
    CardService cardService;

    /**
     * Lists credit cards for a given account with pagination.
     * <p>
     * COBOL source: COCRDLIC.CBL - PROCESS-PAGE-FORWARD paragraph.
     * Original browses CARDDAT VSAM file by account ID.
     * </p>
     *
     * @param accountId the account identifier to filter cards
     * @param page      the page number (0-based)
     * @param size      the page size
     * @return paginated list of credit cards
     */
    @GET
    public Response listCards(
            @QueryParam("accountId") String accountId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        CardListResponse response = cardService.listByAccount(accountId, page, size);
        return Response.ok(response).build();
    }

    /**
     * Retrieves a credit card by its card number.
     * <p>
     * COBOL source: COCRDUPC.CBL - READ-CARD-DATA paragraph.
     * Original reads CARDDAT VSAM file by card number key.
     * </p>
     *
     * @param cardNumber the credit card number
     * @return the credit card details
     */
    @GET
    @Path("/{cardNumber}")
    public Response getCard(@PathParam("cardNumber") String cardNumber) {
        CreditCardDto card = cardService.getByCardNumber(cardNumber);
        if (card == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of("error", "Card not found"))
                    .build();
        }
        return Response.ok(card).build();
    }

    /**
     * Updates a credit card's details.
     * <p>
     * COBOL source: COCRDUPC.CBL - UPDATE-CARD-DATA paragraph.
     * Original rewrites CARDDAT VSAM record after validation.
     * </p>
     *
     * @param cardNumber the credit card number to update
     * @param request    the update request data
     * @return the updated credit card details
     */
    @PUT
    @Path("/{cardNumber}")
    public Response updateCard(
            @PathParam("cardNumber") String cardNumber,
            CardUpdateRequest request) {
        CreditCardDto updated = cardService.updateCard(cardNumber, request);
        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of("error", "Card not found"))
                    .build();
        }
        return Response.ok(updated).build();
    }
}
