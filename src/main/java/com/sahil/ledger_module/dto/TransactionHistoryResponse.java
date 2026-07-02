package com.sahil.ledger_module.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sahil.ledger_module.model.TransactionType;

public record TransactionHistoryResponse(

        Long id,
        BigDecimal amount,
        TransactionType type,
        String description,
        LocalDateTime createdAt

) {
}