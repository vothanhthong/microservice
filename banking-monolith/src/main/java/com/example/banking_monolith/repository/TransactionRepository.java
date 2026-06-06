package com.example.banking_monolith.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.banking_monolith.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySourceAccountNumber(String sourceAccountNumber);
    List<Transaction> findByDestinationAccountNumber(String destinationAccountNumber);
}
