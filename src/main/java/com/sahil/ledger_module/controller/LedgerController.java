package com.sahil.ledger_module.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sahil.ledger_module.dto.AccountRequest;
import com.sahil.ledger_module.model.Account;
import com.sahil.ledger_module.model.TransactionHistory;
import com.sahil.ledger_module.repository.AccountRepository;
import com.sahil.ledger_module.repository.TransactionHistoryRepository;
import com.sahil.ledger_module.service.LedgerService;
import com.sahil.ledger_module.service.ValidationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/ledger")
@Tag(name = "Ledger Management", description = "Operations for validating transactions and transferring funds")
public class LedgerController {

    @Autowired
    private ValidationService validationService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionHistoryRepository historyRepository;

    @Operation(summary = "Validate Double-Entry", description = "Checks if the sum of debits and credits equals zero")
    @ApiResponse(responseCode = "200", description = "Transaction is balanced")
    @PostMapping("/validate")
    public ResponseEntity<String> validateTransaction(@RequestBody Map<String, BigDecimal> entries) {
        validationService.validateTransaction(entries);
        return ResponseEntity.ok("Transaction is valid and balanced.");
    }

    @Operation(summary = "Perform Money Transfer", description = "Moves funds with pessimistic locking and creates audit logs")
    @ApiResponse(responseCode = "200", description = "Transfer successful")
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestParam String from, 
                                         @RequestParam String to, 
                                         @RequestParam BigDecimal amount) {
        ledgerService.transferMoney(from, to, amount);
        return ResponseEntity.ok("Transfer successful and audit log created.");
    }

    @Operation(summary = "Get Account Details", description = "Fetch the current balance for a specific account name")
    @GetMapping("/account/{name}")
    public ResponseEntity<Account> getAccount(@PathVariable String name) {
        // Uses the NON-LOCKING method for better performance
        return accountRepository.findByAccountName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get Transaction History", description = "Fetch all debit/credit records for an account ID")
    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<TransactionHistory>> getHistory(@PathVariable Long accountId) {
        List<TransactionHistory> history = historyRepository.findByAccountId(accountId);
        
        // Correctly returns 404 if the list is empty
        if (history.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(history);
    }

    @PostMapping("/account")
    public ResponseEntity<String> createAccount(@RequestBody AccountRequest request) {
        ledgerService.createAccount(request.getAccountName(), request.getBalance());
        return ResponseEntity.ok("Account created successfully");
    }
}
