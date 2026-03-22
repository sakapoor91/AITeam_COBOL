package com.scalefirstai.evolution.carddemo.cards.dto;

import java.util.List;

/**
 * Paginated response DTO for credit card listings.
 * <p>
 * Modernized from COBOL program COCRDLIC.CBL screen pagination.
 * Original uses CICS screen pages; this uses REST pagination.
 * </p>
 *
 * @param cards      the list of credit card DTOs for this page
 * @param page       the current page number (0-based)
 * @param totalPages the total number of pages
 * @param totalItems the total number of cards matching the query
 */
public record CardListResponse(
        List<CreditCardDto> cards,
        int page,
        int totalPages,
        int totalItems
) {
}
