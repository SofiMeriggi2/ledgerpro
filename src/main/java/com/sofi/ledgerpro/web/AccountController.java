package com.sofi.ledgerpro.web;

import com.sofi.ledgerpro.model.Account;
import com.sofi.ledgerpro.model.LedgerEntry;
import com.sofi.ledgerpro.repo.AccountRepository;
import com.sofi.ledgerpro.repo.LedgerEntryRepository;
import com.sofi.ledgerpro.service.LedgerService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.sofi.ledgerpro.config.JwtAuthFilter.userId;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

  private final AccountRepository accounts;
  private final LedgerEntryRepository entries;
  private final LedgerService ledger;

  public AccountController(AccountRepository accounts,
                           LedgerEntryRepository entries,
                           LedgerService ledger) {
    this.accounts = accounts;
    this.entries = entries;
    this.ledger = ledger;
  }

  // DTOs
  public static record CreateAccountRequest(String name, BigDecimal initialBalance) {}
  public static record CreateEntryRequest(BigDecimal amount, String kind) {}
  public static record TransferRequest(UUID from, UUID to, BigDecimal amount) {}

  // --- Cuentas -------------------------------------------------------------

  @GetMapping
  public List<Account> list(Authentication auth) {
    UUID owner = userId(auth);
    return accounts.findByOwnerIdOrderByCreatedAtDesc(owner);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Account create(Authentication auth, @RequestBody CreateAccountRequest req) {
    UUID owner = userId(auth);

    Account a = new Account();
    a.setOwnerId(owner);
    a.setName(req.name());
    a.setBalance(req.initialBalance() == null ? BigDecimal.ZERO : req.initialBalance());

    var saved = accounts.save(a);
    return accounts.findById(saved.getId()).orElseThrow();
  }

  // --- Movimientos ---------------------------------------------------------

  @GetMapping("/{id}/entries")
  public Page<LedgerEntry> entries(@PathVariable("id") UUID id,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
    if (page < 0) page = 0;
    if (size <= 0) size = 20;
    if (size > 100) size = 100;

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "at"));
    return entries.findByAccount_Id(id, pageable);
  }

  @PostMapping("/{id}/entries")
  @ResponseStatus(HttpStatus.CREATED)
  public LedgerEntry addEntry(@PathVariable("id") UUID id,
                              @RequestBody CreateEntryRequest req) {
    var kind = LedgerEntry.Kind.valueOf(req.kind().toUpperCase());
    return ledger.addEntry(id, req.amount(), kind);
  }

  // --- Transferencias ------------------------------------------------------

  @PostMapping("/transfer")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void transfer(@RequestBody TransferRequest req) {
    ledger.transfer(req.from(), req.to(), req.amount());
  }
}
