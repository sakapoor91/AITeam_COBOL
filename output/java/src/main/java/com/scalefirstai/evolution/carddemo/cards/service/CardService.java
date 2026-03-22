package com.scalefirstai.evolution.carddemo.cards.service;

import com.scalefirstai.evolution.carddemo.cards.dto.CardListResponse;
import com.scalefirstai.evolution.carddemo.cards.dto.CardUpdateRequest;
import com.scalefirstai.evolution.carddemo.cards.dto.CreditCardDto;
import com.scalefirstai.evolution.carddemo.cards.model.CreditCard;
import com.scalefirstai.evolution.carddemo.cards.repository.CardRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for credit card business logic.
 * <p>
 * Modernized from COBOL programs COCRDLIC.CBL and COCRDUPC.CBL.
 * Handles card listing by account, retrieval by card number,
 * and card detail updates with validation.
 * </p>
 */
@ApplicationScoped
public class CardService {

    @Inject
    CardRepository cardRepository;

    /**
     * Lists credit cards for an account with pagination.
     * <p>
     * COBOL source: COCRDLIC.CBL - BROWSE-CARD-FILE paragraph.
     * Original uses STARTBR/READNEXT on CARDDAT VSAM file.
     * </p>
     *
     * @param accountId the account identifier
     * @param page      the page number (0-based)
     * @param size      the page size
     * @return paginated card list response
     */
    public CardListResponse listByAccount(String accountId, int page, int size) {
        List<CreditCard> cards;
        long totalItems;

        if (accountId != null && !accountId.isBlank()) {
            cards = cardRepository.findByAccountId(accountId, page, size);
            totalItems = cardRepository.countByAccountId(accountId);
        } else {
            cards = cardRepository.findAll()
                    .page(page, size)
                    .list();
            totalItems = cardRepository.count();
        }

        List<CreditCardDto> dtos = cards.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) totalItems / size);

        return new CardListResponse(dtos, page, totalPages, (int) totalItems);
    }

    /**
     * Retrieves a credit card by card number.
     * <p>
     * COBOL source: COCRDUPC.CBL - READ-CARD-DATA paragraph.
     * Original reads CARDDAT VSAM by primary key.
     * </p>
     *
     * @param cardNumber the card number to look up
     * @return the card DTO, or null if not found
     */
    public CreditCardDto getByCardNumber(String cardNumber) {
        CreditCard card = cardRepository.findByCardNumber(cardNumber);
        if (card == null) {
            return null;
        }
        return toDto(card);
    }

    /**
     * Updates a credit card's details with validation.
     * <p>
     * COBOL source: COCRDUPC.CBL - UPDATE-CARD-DATA paragraph.
     * Validates card status and expiry before rewriting VSAM record.
     * </p>
     *
     * @param cardNumber the card number to update
     * @param request    the update data
     * @return the updated card DTO, or null if not found
     */
    @Transactional
    public CreditCardDto updateCard(String cardNumber, CardUpdateRequest request) {
        CreditCard card = cardRepository.findByCardNumber(cardNumber);
        if (card == null) {
            return null;
        }

        if (request.cardholderName() != null) {
            card.setCardholderName(request.cardholderName());
        }
        if (request.expiryDate() != null) {
            card.setExpiryDate(request.expiryDate());
        }
        if (request.status() != null) {
            card.setStatus(request.status());
        }

        cardRepository.persist(card);
        return toDto(card);
    }

    private CreditCardDto toDto(CreditCard card) {
        return new CreditCardDto(
                card.getCardNumber(),
                card.getAccountId(),
                card.getCardholderName(),
                card.getExpiryDate(),
                card.getStatus(),
                card.getCreditLimit(),
                card.getCurrentBalance()
        );
    }
}
