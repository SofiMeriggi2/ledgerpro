package com.sofi.ledgerpro.service;

import com.sofi.ledgerpro.model.Account;
import com.sofi.ledgerpro.model.LedgerEntry;
import com.sofi.ledgerpro.repo.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(LedgerService.class)
class LedgerServiceTest {

    @Autowired
    LedgerService ledger;

    @Autowired
    AccountRepository accounts;

    UUID owner;
    UUID secondOwner;
    UUID accountId;

    @BeforeEach
    void setUp() {
        owner = UUID.randomUUID();
        secondOwner = UUID.randomUUID();

        Account primary = new Account();
        primary.setOwnerId(owner);
        primary.setName("Caja");
        primary.setBalance(new BigDecimal("100.00"));
        accountId = accounts.save(primary).getId();
    }

    @Test
    void addEntryDebitInsufficientBalance() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            ledger.addEntry(owner, accountId, new BigDecimal("150.00"), LedgerEntry.Kind.DEBIT)
        );
        assertEquals("Saldo insuficiente", ex.getMessage());
        assertEquals(new BigDecimal("100.00"), accounts.findById(accountId).orElseThrow().getBalance());
    }

    @Test
    void transferSameAccountFails() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            ledger.transfer(owner, accountId, accountId, new BigDecimal("10.00"))
        );
        assertEquals("Las cuentas deben ser distintas", ex.getMessage());
    }

    @Test
    void transferAccountFromDifferentOwnerNotFound() {
        Account foreign = new Account();
        foreign.setOwnerId(secondOwner);
        foreign.setName("Externo");
        foreign.setBalance(new BigDecimal("25.00"));
        UUID foreignId = accounts.save(foreign).getId();

        assertThrowsExactly(java.util.NoSuchElementException.class, () ->
            ledger.transfer(owner, accountId, foreignId, new BigDecimal("10.00"))
        );
    }
}
