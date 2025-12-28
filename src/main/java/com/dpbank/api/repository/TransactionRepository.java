package com.dpbank.api.repository;

import com.dpbank.api.domain.Transaction;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persists each processed {@link Transaction} so statements and audits can be
 * rebuilt from the database.
 */
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
}
