package com.sahil.ledger_module.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String accountName) {
        super("Account not found: " + accountName);
    }
}