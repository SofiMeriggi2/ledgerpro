package com.sofi.ledgerpro.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

  @Id @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal balance = BigDecimal.ZERO;

  // 🔒 Dueña de la cuenta (usuario autenticado)
  @Column(name = "owner_id") // no lo marco NOT NULL por si tenés filas viejas
  private UUID ownerId;

  @Column(name = "created_at", nullable = false, columnDefinition = "timestamp with time zone")
  private OffsetDateTime createdAt;

  @PrePersist
  public void prePersist() {
    if (createdAt == null) createdAt = OffsetDateTime.now();
    if (balance == null) balance = BigDecimal.ZERO;
  }

  // Getters & Setters
  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public BigDecimal getBalance() { return balance; }
  public void setBalance(BigDecimal balance) { this.balance = balance; }

  public UUID getOwnerId() { return ownerId; }
  public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
