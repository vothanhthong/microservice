package com.example.banking_monolith.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.banking_monolith.model.Account;
import com.example.banking_monolith.model.Transaction;
import com.example.banking_monolith.repository.AccountRepository;
import com.example.banking_monolith.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Executes a money transfer between two accounts atomically.
     *
     * @Transactional ensures that if any part of the transfer fails (e.g. database crash,
     * business validation exception), the entire database state is rolled back so no money is lost.
     */
    @Transactional
    public Transaction transfer(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount) {
        // 1. Validation checks
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero.");
        }

        if (sourceAccountNumber.equals(destinationAccountNumber)) {
            throw new IllegalArgumentException("Cannot transfer money to the same account.");
        }

        // 2. Fetch both accounts, throwing an error if they don't exist
        Account sourceAccount = accountRepository.findByAccountNumber(sourceAccountNumber)
                .orElseThrow(() -> new RuntimeException("Sender account not found: " + sourceAccountNumber));

        Account destinationAccount = accountRepository.findByAccountNumber(destinationAccountNumber)
                .orElseThrow(() -> new RuntimeException("Receiver account not found: " + destinationAccountNumber));

        // 3. Verify sender has sufficient funds
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance in account: " + sourceAccountNumber);
        }

        // 4. Update balances
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));

        // 5. Save the updated accounts back to H2 database
        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        // 6. Log the transaction ledger entry
        Transaction transaction = new Transaction(
            sourceAccountNumber,
            destinationAccountNumber,
            amount,
            LocalDateTime.now()
        );
        
        return transactionRepository.save(transaction);
    }

    /**
     * Retrieves the combined history of all sent and received transactions for an account,
     * sorted from newest to oldest.
     *
     * Implementing Option 4: We fetch sent and received lists separately from the database,
     * concatenate them, and sort them by timestamp in memory using Java Streams.
     */
    public List<Transaction> getTransactionHistory(String accountNumber) {
        List<Transaction> sentTransactions = transactionRepository.findBySourceAccountNumber(accountNumber);
        List<Transaction> receivedTransactions = transactionRepository.findByDestinationAccountNumber(accountNumber);

        // Combine sent and received transactions, then sort by timestamp descending
        return Stream.concat(sentTransactions.stream(), receivedTransactions.stream())
                .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()))
                .collect(Collectors.toList());
    }
}
