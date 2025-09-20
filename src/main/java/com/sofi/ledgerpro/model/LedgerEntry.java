package com.sofi.ledgerpro.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.time.ZoneOffset;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {
    public enum Kind { CREDIT, DEBIT }

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Kind kind;

    @CreationTimestamp
    @Column(name = "at", nullable = false)
    private OffsetDateTime at;

    // getters y setters
    public UUID getId() { return id; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Kind getKind() { return kind; }
    public void setKind(Kind kind) { this.kind = kind; }
    public OffsetDateTime getAt() { return at; }
    public void setAt(OffsetDateTime at) { this.at = at; }

    @PrePersist
    void prePersist() {
        if (at == null) {
            at = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }
}
