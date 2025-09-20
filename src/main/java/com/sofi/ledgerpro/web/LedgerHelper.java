package com.sofi.ledgerpro.web;

import com.sofi.ledgerpro.model.Account;
import com.sofi.ledgerpro.model.LedgerEntry;
import com.sofi.ledgerpro.model.LedgerEntry.Kind;
import com.sofi.ledgerpro.repo.AccountRepository;
import com.sofi.ledgerpro.repo.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class LedgerHelper {
    private final AccountRepository accounts;
    private final LedgerEntryRepository entries;

    public LedgerHelper(AccountRepository accounts, LedgerEntryRepository entries) {
        this.accounts = accounts;
        this.entries = entries;
    }

    @Transactional
    public LedgerEntry addEntry(UUID accountId, BigDecimal amount, Kind kind) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount debe ser > 0");
        }
        Account acc = accounts.findById(accountId).orElseThrow();

        BigDecimal newBal = (kind == Kind.CREDIT)
                ? acc.getBalance().add(amount)
                : acc.getBalance().subtract(amount);

        if (newBal.signum() < 0) {
            throw new IllegalStateException("saldo insuficiente");
        }
        acc.setBalance(newBal);

        LedgerEntry e = new LedgerEntry();
        e.setAccount(acc);
        e.setAmount(amount);
        e.setKind(kind);

        return entries.save(e); // at lo completa la DB
    }

    @Transactional
    public void transfer(UUID from, UUID to, BigDecimal amount) {
        if (from.equals(to)) throw new IllegalArgumentException("cuentas iguales");
        addEntry(from, amount, Kind.DEBIT);
        addEntry(to, amount, Kind.CREDIT);
    }
}
