package com.sahil.ledger_module.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sahil.ledger_module.exception.InconsistentDataException;
import com.sahil.ledger_module.repository.AccountRepository;

@Service
public class ValidationService {

    private final AccountRepository accountRepository;

    public ValidationService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void validateTransaction(Map<String, BigDecimal> entries) { // Changed Double to BigDecimal
     
        // Summing BigDecimals using Stream
        BigDecimal totalSum = entries.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSum.compareTo(BigDecimal.ZERO) != 0) {
            throw new InconsistentDataException("Imbalance: Sum is " + totalSum);
        }

        for (Map.Entry<String, BigDecimal> entry : entries.entrySet()) {
            String name = entry.getKey();
            BigDecimal amount = entry.getValue();

            accountRepository.findByAccountName(name).ifPresent(account -> {
                // Using .add() and .compareTo() for BigDecimal safety
                if (account.getBalance().add(amount).compareTo(BigDecimal.ZERO) < 0) {
                    throw new InconsistentDataException("Security Alert: Account [" + name + "] cannot have a negative balance!");
                }
            });
        }
    }
}