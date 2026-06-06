package com.example.banking_monolith.service;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.example.banking_monolith.model.Account;
import com.example.banking_monolith.repository.AccountRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    /**
     * Creates a new banking account with a random generated account number.
     */
    public Account createAccount(String ownerName, BigDecimal initialBalance) {
        // Generate a random unique account number like: ACC-A1B2
        String accountNumber = "ACC-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        
        Account account = new Account(accountNumber, ownerName, initialBalance);
        return accountRepository.save(account);
    }

    /**
     * Retrieves an account by its unique account number, or throws a RuntimeException.
     */
    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found with number: " + accountNumber));
    }
}
