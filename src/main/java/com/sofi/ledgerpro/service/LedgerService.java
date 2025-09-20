package com.sofi.ledgerpro.service;

import com.sofi.ledgerpro.model.Account;
import com.sofi.ledgerpro.model.LedgerEntry;
import com.sofi.ledgerpro.repo.AccountRepository;
import com.sofi.ledgerpro.repo.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class LedgerService {

    private final AccountRepository accounts;
    private final LedgerEntryRepository entries;

    public LedgerService(AccountRepository accounts, LedgerEntryRepository entries) {
        this.accounts = accounts;
        this.entries = entries;
    }

    @Transactional
    public LedgerEntry addEntry(UUID accountId, BigDecimal amount, LedgerEntry.Kind kind) {
        Account acc = accounts.findById(accountId).orElseThrow();
        if (kind == LedgerEntry.Kind.DEBIT) {
            acc.setBalance(acc.getBalance().subtract(amount));
        } else {
            acc.setBalance(acc.getBalance().add(amount));
        }
        LedgerEntry e = new LedgerEntry();
        e.setAccount(acc);
        e.setAmount(amount);
        e.setKind(kind);
        // el campo 'at' lo completa Postgres por defecto
        accounts.save(acc);
        return entries.save(e);
    }

    @Transactional
    public void transfer(UUID from, UUID to, BigDecimal amount) {
        addEntry(from, amount, LedgerEntry.Kind.DEBIT);
        addEntry(to, amount, LedgerEntry.Kind.CREDIT);
    }
}
