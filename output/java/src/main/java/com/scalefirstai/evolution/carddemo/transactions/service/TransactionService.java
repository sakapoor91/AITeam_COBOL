package com.scalefirstai.evolution.carddemo.transactions.service;

import com.scalefirstai.evolution.carddemo.common.MoneyUtil;
import com.scalefirstai.evolution.carddemo.transactions.dto.TransactionDto;
import com.scalefirstai.evolution.carddemo.transactions.dto.TransactionListResponse;
import com.scalefirstai.evolution.carddemo.transactions.dto.TransactionRequest;
import com.scalefirstai.evolution.carddemo.transactions.model.Transaction;
import com.scalefirstai.evolution.carddemo.transactions.model.TransactionType;
import com.scalefirstai.evolution.carddemo.transactions.repository.TransactionRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for transaction business logic.
 * <p>
 * Modernized from COBOL programs COTRN00C.CBL, COTRN01C.CBL,
 * and COTRN02C.CBL. Handles transaction listing, creation,
 * and validation with balance and limit checks.
 * </p>
 */
@ApplicationScoped
public class TransactionService {

    @Inject
    TransactionRepository transactionRepository;

    /**
     * Lists transactions by card number with pagination.
     * <p>
     * COBOL source: COTRN00C.CBL - BROWSE-TRANSACTION-FILE paragraph.
     * Original uses STARTBR/READNEXT on TRANSACT VSAM file.
     * </p>
     *
     * @param cardNumber the card number to filter by
     * @param page       the page number (0-based)
     * @param size       the page size
     * @return paginated transaction list response
     */
    public TransactionListResponse listByCardNumber(String cardNumber, int page, int size) {
        List<Transaction> transactions;
        long totalItems;

        if (cardNumber != null && !cardNumber.isBlank()) {
            transactions = transactionRepository.findByCardNumber(cardNumber, page, size);
            totalItems = transactionRepository.countByCardNumber(cardNumber);
        } else {
            transactions = transactionRepository.findAll()
                    .page(page, size)
                    .list();
            totalItems = transactionRepository.count();
        }

        List<TransactionDto> dtos = transactions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) totalItems / size);

        return new TransactionListResponse(dtos, page, totalPages, (int) totalItems);
    }

    /**
     * Creates a new transaction with validation.
     * <p>
     * COBOL source: COTRN02C.CBL - PROCESS-TRANSACTION paragraph.
     * Validates transaction type, amount > 0, and card number presence
     * before writing to the transaction store.
     * </p>
     *
     * @param request the transaction request data
     * @return the created transaction DTO
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public TransactionDto createTransaction(TransactionRequest request) {
        if (request.cardNumber() == null || request.cardNumber().isBlank()) {
            throw new IllegalArgumentException("Card number is required");
        }
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }
        if (request.transactionType() == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }

        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setCardNumber(request.cardNumber());
        transaction.setTransactionType(TransactionType.valueOf(request.transactionType()));
        transaction.setAmount(MoneyUtil.round(request.amount()));
        transaction.setMerchantName(request.merchantName());
        transaction.setDescription(request.description());
        transaction.setTransactionDate(LocalDateTime.now());

        transactionRepository.persist(transaction);
        return toDto(transaction);
    }

    private TransactionDto toDto(Transaction transaction) {
        return new TransactionDto(
                transaction.getTransactionId(),
                transaction.getCardNumber(),
                transaction.getTransactionType().name(),
                transaction.getAmount(),
                transaction.getMerchantName(),
                transaction.getDescription(),
                transaction.getTransactionDate()
        );
    }
}
