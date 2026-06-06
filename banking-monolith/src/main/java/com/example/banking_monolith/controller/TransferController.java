package com.example.banking_monolith.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.banking_monolith.dto.TransferRequestDTO;
import com.example.banking_monolith.model.Transaction;
import com.example.banking_monolith.service.TransferService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    /**
     * Endpoint to execute a funds transfer between two accounts.
     * Returns 200 OK on success containing the logged transaction ledger record.
     */
    @PostMapping
    public ResponseEntity<Transaction> transferFunds(@Valid @RequestBody TransferRequestDTO request) {
        Transaction transaction = transferService.transfer(
            request.sourceAccountNumber(),
            request.destinationAccountNumber(),
            request.amount()
        );
        return ResponseEntity.ok(transaction);
    }

    /**
     * Endpoint to retrieve the transaction history of a specific account (both sent and received).
     * Returns 200 OK containing the sorted transaction ledger list.
     */
    @GetMapping("/history/{accountNumber}")
    public ResponseEntity<List<Transaction>> getHistory(@PathVariable String accountNumber) {
        List<Transaction> history = transferService.getTransactionHistory(accountNumber);
        return ResponseEntity.ok(history);
    }
}
