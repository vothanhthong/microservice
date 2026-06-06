package com.example.banking_monolith.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.banking_monolith.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Optional<Account> findByAccountNumber(String accountNumber);
}