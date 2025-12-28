package com.dpbank.api.repository;

import com.dpbank.api.domain.Account;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Data access layer for {@link Account} aggregates including helpers that
 * enforce pessimistic locking for concurrent launches.
 */
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Fetches an account acquiring a {@code PESSIMISTIC_WRITE} lock to serialize
     * concurrent transaction batches.
     *
     * @param id account identifier
     * @return locked account if it exists
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdWithLock(@Param("id") UUID id);
}
