package com.sofi.ledgerpro.service;

import com.sofi.ledgerpro.model.Account;
import com.sofi.ledgerpro.model.LedgerEntry;
import com.sofi.ledgerpro.repo.AccountRepository;
import com.sofi.ledgerpro.repo.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Objects;
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
    public LedgerEntry addEntry(UUID ownerId, UUID accountId, BigDecimal amount, LedgerEntry.Kind kind) {
        UUID owner = Objects.requireNonNull(ownerId, "El propietario es requerido");
        UUID account = Objects.requireNonNull(accountId, "La cuenta es requerida");
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        LedgerEntry.Kind entryKind = Objects.requireNonNull(kind, "El tipo de movimiento es requerido");

        Account acc = accounts.findByIdAndOwnerId(account, owner)
            .orElseThrow(NoSuchElementException::new);

        BigDecimal newBalance = entryKind == LedgerEntry.Kind.CREDIT
            ? acc.getBalance().add(amount)
            : acc.getBalance().subtract(amount);

        if (newBalance.signum() < 0) {
            throw new IllegalStateException("Saldo insuficiente");
        }

        acc.setBalance(newBalance);

        LedgerEntry e = new LedgerEntry();
        e.setAccount(acc);
        e.setAmount(amount);
        e.setKind(entryKind);

        return entries.save(e);
    }

    @Transactional
    public void transfer(UUID ownerId, UUID from, UUID to, BigDecimal amount) {
        UUID owner = Objects.requireNonNull(ownerId, "El propietario es requerido");
        UUID origin = Objects.requireNonNull(from, "La cuenta origen es requerida");
        UUID target = Objects.requireNonNull(to, "La cuenta destino es requerida");
        if (origin.equals(target)) {
            throw new IllegalArgumentException("Las cuentas deben ser distintas");
        }

        addEntry(owner, origin, amount, LedgerEntry.Kind.DEBIT);
        addEntry(owner, target, amount, LedgerEntry.Kind.CREDIT);
    }
}
