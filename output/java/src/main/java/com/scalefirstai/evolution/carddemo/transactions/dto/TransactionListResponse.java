package com.scalefirstai.evolution.carddemo.transactions.dto;

import java.util.List;

/**
 * Paginated response DTO for transaction listings.
 * <p>
 * Modernized from COBOL program COTRN00C.CBL screen pagination.
 * Original uses CICS screen pages; this uses REST pagination.
 * </p>
 *
 * @param transactions the list of transaction DTOs for this page
 * @param page         the current page number (0-based)
 * @param totalPages   the total number of pages
 * @param totalItems   the total number of transactions matching the query
 */
public record TransactionListResponse(
        List<TransactionDto> transactions,
        int page,
        int totalPages,
        int totalItems
) {
}
