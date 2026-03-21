package com.sahil.ledger_module.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AccountRequest {
    private String accountName;
    private BigDecimal balance;
}