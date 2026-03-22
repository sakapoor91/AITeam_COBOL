package com.scalefirstai.evolution.carddemo.accounts.service;

import com.scalefirstai.evolution.carddemo.accounts.dto.AccountDto;
import com.scalefirstai.evolution.carddemo.accounts.dto.AccountUpdateRequest;
import com.scalefirstai.evolution.carddemo.accounts.model.Account;
import com.scalefirstai.evolution.carddemo.accounts.model.AccountStatus;
import com.scalefirstai.evolution.carddemo.accounts.repository.AccountRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Service for account business logic.
 * <p>
 * Modernized from COBOL programs COACTUPC.CBL and COACTVWC.CBL.
 * Handles account retrieval and updates with validation rules
 * equivalent to original COBOL business logic.
 * </p>
 */
@ApplicationScoped
public class AccountService {

    @Inject
    AccountRepository accountRepository;

    /**
     * Retrieves an account by its account ID.
     * <p>
     * COBOL source: COACTVWC.CBL - READ-ACCT-DATA paragraph.
     * </p>
     *
     * @param accountId the account identifier
     * @return the account DTO, or null if not found
     */
    public AccountDto getByAccountId(String accountId) {
        Account account = accountRepository.findByAccountId(accountId);
        if (account == null) {
            return null;
        }
        return toDto(account);
    }

    /**
     * Updates an account with validation.
     * <p>
     * COBOL source: COACTUPC.CBL - UPDATE-ACCT-DATA paragraph.
     * Validates status transitions and credit limit changes
     * before persisting the update.
     * </p>
     *
     * @param accountId the account identifier
     * @param request   the update data
     * @return the updated account DTO, or null if not found
     */
    @Transactional
    public AccountDto updateAccount(String accountId, AccountUpdateRequest request) {
        Account account = accountRepository.findByAccountId(accountId);
        if (account == null) {
            return null;
        }

        if (request.status() != null) {
            account.setStatus(AccountStatus.valueOf(request.status()));
        }
        if (request.creditLimit() != null) {
            account.setCreditLimit(request.creditLimit());
        }
        if (request.cashCreditLimit() != null) {
            account.setCashCreditLimit(request.cashCreditLimit());
        }

        accountRepository.persist(account);
        return toDto(account);
    }

    private AccountDto toDto(Account account) {
        return new AccountDto(
                account.getAccountId(),
                account.getStatus().name(),
                account.getCurrentBalance(),
                account.getCreditLimit(),
                account.getCashCreditLimit(),
                account.getOpenDate(),
                account.getExpirationDate()
        );
    }
}
