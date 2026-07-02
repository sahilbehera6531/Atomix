package com.sahil.ledger_module.dto;

import java.math.BigDecimal;

public record AccountResponse(Long id, String accountName, BigDecimal balance){
    
}