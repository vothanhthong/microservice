package com.example.banking_monolith.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.banking_monolith.dto.AccountRequestDTO;
import com.example.banking_monolith.model.Account;
import com.example.banking_monolith.service.AccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * Endpoint to create a new bank account.
     * Returns 201 Created on success.
     */
    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody AccountRequestDTO request) {
        Account createdAccount = accountService.createAccount(
            request.ownerName(), 
            request.initialBalance()
        );
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    /**
     * Endpoint to retrieve account details by account number.
     * Returns 200 OK on success.
     */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountNumber) {
        Account account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(account);
    }
}
