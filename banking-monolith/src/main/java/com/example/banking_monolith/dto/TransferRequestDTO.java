package com.example.banking_monolith.dto;

import java.math.BigDecimal;

/**
 * A modern Java Record representing the payload to execute a bank transfer.
 */
public record TransferRequestDTO(
    String sourceAccountNumber,
    String destinationAccountNumber,
    BigDecimal amount
) {}
