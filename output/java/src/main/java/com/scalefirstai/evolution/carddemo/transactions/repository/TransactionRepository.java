package com.scalefirstai.evolution.carddemo.transactions.repository;

import com.scalefirstai.evolution.carddemo.transactions.model.Transaction;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Repository for Transaction entity persistence operations.
 * <p>
 * Modernized from COBOL VSAM BROWSE/READ/WRITE operations on TRANSACT file
 * in programs COTRN00C.CBL, COTRN01C.CBL, and COTRN02C.CBL.
 * </p>
 */
@ApplicationScoped
public class TransactionRepository implements PanacheRepositoryBase<Transaction, String> {

    /**
     * Finds transactions by card number with pagination.
     * <p>
     * COBOL source: COTRN00C.CBL - STARTBR/READNEXT by TRAN-CARD-NUM.
     * </p>
     *
     * @param cardNumber the card number to filter by
     * @param page       the page number (0-based)
     * @param size       the page size
     * @return list of transactions for the card
     */
    public List<Transaction> findByCardNumber(String cardNumber, int page, int size) {
        return find("cardNumber", cardNumber)
                .page(page, size)
                .list();
    }

    /**
     * Counts transactions for a given card number.
     *
     * @param cardNumber the card number to count transactions for
     * @return the total count of transactions for the card
     */
    public long countByCardNumber(String cardNumber) {
        return count("cardNumber", cardNumber);
    }
}
