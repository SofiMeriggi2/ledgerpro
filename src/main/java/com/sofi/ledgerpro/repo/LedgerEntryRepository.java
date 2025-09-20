package com.sofi.ledgerpro.repo;

import com.sofi.ledgerpro.model.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    Page<LedgerEntry> findByAccount_Id(UUID accountId, Pageable pageable);
}
