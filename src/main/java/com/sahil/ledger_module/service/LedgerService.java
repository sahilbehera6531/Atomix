package com.sahil.ledger_module.service;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    private static final Logger logger = LoggerFactory.getLogger(LedgerService.class);

    @Transactional
    public void transferMoney(String fromAccountName, String toAccountName, BigDecimal amount) {

        logger.info("Initiating transfer from {} to {} of amount {}", fromAccountName, toAccountName, amount);
        
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
        logger.info("Transfer successful between {} and {}", fromAccountName, toAccountName);
    }

    public void createAccount(String name, BigDecimal balance) {
        Account account = new Account();
        account.setAccountName(name);
        account.setBalance(balance);
        accountRepository.save(account);
    }

    public List<TransactionHistory> getFilteredHistory(Long accountId, TransactionType type) {
        return historyRepository.findByAccountIdAndType(accountId, type);
    }

    public Page<TransactionHistory> getPaginatedHistory(Long accountId, Pageable pageable) {
        return historyRepository.findByAccountId(accountId, pageable);
    }
    
}

