package com.scalefirstai.evolution.carddemo.cards.repository;

import com.scalefirstai.evolution.carddemo.cards.model.CreditCard;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Repository for CreditCard entity persistence operations.
 * <p>
 * Modernized from COBOL VSAM BROWSE/READ operations on CARDDAT file
 * in programs COCRDLIC.CBL and COCRDUPC.CBL.
 * </p>
 */
@ApplicationScoped
public class CardRepository implements PanacheRepositoryBase<CreditCard, String> {

    /**
     * Finds a credit card by its card number.
     * <p>
     * COBOL source: COCRDUPC.CBL - READ CARDDAT-FILE KEY IS CARD-NUM.
     * </p>
     *
     * @param cardNumber the card number to search
     * @return the CreditCard entity, or null if not found
     */
    public CreditCard findByCardNumber(String cardNumber) {
        return findById(cardNumber);
    }

    /**
     * Finds cards by account ID with pagination.
     * <p>
     * COBOL source: COCRDLIC.CBL - STARTBR/READNEXT by CARD-ACCT-ID.
     * </p>
     *
     * @param accountId the account identifier
     * @param page      the page number (0-based)
     * @param size      the page size
     * @return list of cards for the account
     */
    public List<CreditCard> findByAccountId(String accountId, int page, int size) {
        return find("accountId", accountId)
                .page(page, size)
                .list();
    }

    /**
     * Counts cards for a given account.
     *
     * @param accountId the account identifier
     * @return the total count of cards for the account
     */
    public long countByAccountId(String accountId) {
        return count("accountId", accountId);
    }
}
