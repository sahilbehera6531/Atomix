package com.sahil.ledger_module.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sahil.ledger_module.model.TransactionHistory;
import com.sahil.ledger_module.model.TransactionType;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {
    List<TransactionHistory> findByAccountId(Long accountId);
    List<TransactionHistory> findByAccountIdAndType(Long accountId, TransactionType type);

    Page<TransactionHistory> findByAccountId(Long accountId, Pageable pageable);
}

