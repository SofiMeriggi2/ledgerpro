package com.sofi.ledgerpro.repo;

import com.sofi.ledgerpro.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

  Page<Account> findByNameContainingIgnoreCase(String q, Pageable pageable);

  List<Account> findByNameIgnoreCase(String name);

  // 🔒 listar sólo las del dueño
  List<Account> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
}
