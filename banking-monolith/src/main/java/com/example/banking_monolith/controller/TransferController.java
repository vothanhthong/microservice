package com.example.banking_monolith.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.banking_monolith.dto.TransferRequestDTO;
import com.example.banking_monolith.model.Transaction;
import com.example.banking_monolith.model.IdempotentRequest;
import com.example.banking_monolith.service.TransferService;
import com.example.banking_monolith.service.IdempotencyService;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final IdempotencyService idempotencyService;

    /**
     * Endpoint to execute a funds transfer between two accounts.
     * Returns 200 OK on success containing the logged transaction ledger record.
     * Required Idempotency-Key header for safe retries.
     */
    @PostMapping
    public ResponseEntity<?> transferFunds(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @Valid @RequestBody TransferRequestDTO request) {
        
        if (idempotencyKeyHeader == null || idempotencyKeyHeader.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required.");
        }

        UUID idempotencyKey;
        try {
            idempotencyKey = UUID.fromString(idempotencyKeyHeader);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Idempotency-Key header must be a valid UUID.");
        }

        // Check cache
        Optional<IdempotentRequest> cachedRequest = idempotencyService.getCachedRequest(idempotencyKey);
        if (cachedRequest.isPresent()) {
            IdempotentRequest cached = cachedRequest.get();
            Transaction transaction = idempotencyService.deserializeResponse(cached.getResponseBody(), Transaction.class);
            return ResponseEntity.status(cached.getStatusCode()).body(transaction);
        }

        // Execute transfer
        Transaction transaction = transferService.transfer(
            request.sourceAccountNumber(),
            request.destinationAccountNumber(),
            request.amount()
        );

        // Cache response
        idempotencyService.saveResponse(idempotencyKey, transaction, 200);

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
