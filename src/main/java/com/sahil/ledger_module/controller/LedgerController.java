package com.sahil.ledger_module.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
import com.sahil.ledger_module.model.TransactionType;
import com.sahil.ledger_module.service.LedgerService;
import com.sahil.ledger_module.service.ValidationService;
import com.sahil.ledger_module.dto.AccountResponse;
import com.sahil.ledger_module.dto.TransactionHistoryResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;




@RestController
@RequestMapping("/api/ledger")
@Tag(name = "Ledger Management", description = "Operations for validating transactions and transferring funds")
public class LedgerController {

    private final ValidationService validationService;

    private final LedgerService ledgerService;

    public LedgerController(ValidationService validationService,
                            LedgerService ledgerService) {

        this.validationService = validationService;
        this.ledgerService = ledgerService;
    }

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
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String name) {

        return ResponseEntity.ok(
                ledgerService.getAccount(name)
        );
    }

    @Operation(summary = "Get Transaction History", description = "Fetch all debit/credit records for an account ID")
    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<TransactionHistoryResponse>> getHistory(@PathVariable Long accountId) {

        List<TransactionHistoryResponse> history = ledgerService.getHistory(accountId);

        if (history.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Create account", description = "Creates a new account with initial balance")
    @ApiResponse(responseCode = "200", description = "Account created successfully")
    @PostMapping("/account")
    public ResponseEntity<String> createAccount(@Valid @RequestBody AccountRequest request) {
        ledgerService.createAccount(request.getAccountName(), request.getBalance());
        return ResponseEntity
        .status(HttpStatus.CREATED)
        .body("Account created successfully");
    }

    @Operation(summary = "Filter transaction history", description = "Fetch transaction history filtered by type")
    @ApiResponse(responseCode = "200", description = "Filtered transactions fetched successfully")
    @GetMapping("/history/filter")
    public ResponseEntity<List<TransactionHistoryResponse>> getFilteredHistory(
            @RequestParam Long accountId,
            @RequestParam TransactionType type) {

        return ResponseEntity.ok(
                ledgerService.getFilteredHistory(accountId, type)
        );
    }

   @Operation(summary = "Paginated transaction history", description = "Fetch transaction history with pagination support")
    @ApiResponse(responseCode = "200", description = "Paginated data fetched successfully")
    @GetMapping("/history/page")
    public ResponseEntity<Page<TransactionHistoryResponse>> getPaginatedHistory(
            @RequestParam Long accountId,
            Pageable pageable) {

        return ResponseEntity.ok(
                ledgerService.getPaginatedHistory(accountId, pageable)
        );
    }
}
