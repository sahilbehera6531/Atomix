package com.sahil.ledger_module.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AccountRequest {
    @NotBlank
    private String accountName;

    @NotNull
    @Positive
    private BigDecimal balance;
}