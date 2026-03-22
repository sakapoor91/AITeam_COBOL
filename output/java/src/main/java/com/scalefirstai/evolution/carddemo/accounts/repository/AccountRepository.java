package com.scalefirstai.evolution.carddemo.accounts.repository;

import com.scalefirstai.evolution.carddemo.accounts.model.Account;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository for Account entity persistence operations.
 * <p>
 * Modernized from COBOL VSAM READ/REWRITE operations on ACCTDAT file
 * in programs COACTUPC.CBL and COACTVWC.CBL.
 * </p>
 */
@ApplicationScoped
public class AccountRepository implements PanacheRepositoryBase<Account, String> {

    /**
     * Finds an account by its account ID.
     * <p>
     * COBOL source: COACTVWC.CBL - READ ACCTDAT-FILE KEY IS ACCT-ID.
     * </p>
     *
     * @param accountId the account identifier to search
     * @return the Account entity, or null if not found
     */
    public Account findByAccountId(String accountId) {
        return findById(accountId);
    }
}
