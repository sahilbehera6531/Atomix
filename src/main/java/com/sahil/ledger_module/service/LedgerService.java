package com.sahil.ledger_module.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sahil.ledger_module.exception.InsufficientFundsException;
import com.sahil.ledger_module.model.Account;
import com.sahil.ledger_module.model.TransactionHistory;
import com.sahil.ledger_module.model.TransactionType;
import com.sahil.ledger_module.repository.AccountRepository;
import com.sahil.ledger_module.repository.TransactionHistoryRepository;

@Service
public class LedgerService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionHistoryRepository historyRepository;

    @Transactional
    public void transferMoney(String fromAccountName, String toAccountName, BigDecimal amount) {
        
        // CRITICAL CHANGE: Use findByAccountNameWithLock to trigger Pessimistic Locking
        Account fromAccount = accountRepository.findByAccountNameWithLock(fromAccountName)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        Account toAccount = accountRepository.findByAccountNameWithLock(toAccountName)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for account: " + fromAccountName);
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        TransactionHistory debitRecord = new TransactionHistory(
            fromAccount, amount.negate(), TransactionType.DEBIT, "Transfer to " + toAccountName);
        
        TransactionHistory creditRecord = new TransactionHistory(
            toAccount, amount, TransactionType.CREDIT, "Transfer from " + fromAccountName);

        historyRepository.save(debitRecord);
        historyRepository.save(creditRecord);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

    public void createAccount(String name, BigDecimal balance) {
        Account account = new Account();
        account.setAccountName(name);
        account.setBalance(balance);
        accountRepository.save(account);
    }
}

