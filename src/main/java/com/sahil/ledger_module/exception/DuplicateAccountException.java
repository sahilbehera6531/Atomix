package com.sahil.ledger_module.exception;

public class DuplicateAccountException extends RuntimeException {

    public DuplicateAccountException(String accountName) {
        super("Account already exists: " + accountName);
    }
}