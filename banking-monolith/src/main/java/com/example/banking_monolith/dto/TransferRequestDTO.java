package com.example.banking_monolith.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * A modern Java Record representing the payload to execute a bank transfer.
 */
public record TransferRequestDTO(
    @NotBlank(message = "Source account number must not be blank")
    String sourceAccountNumber,

    @NotBlank(message = "Destination account number must not be blank")
    String destinationAccountNumber,

    @NotNull(message = "Transfer amount must not be null")
    @Positive(message = "Transfer amount must be positive")
    BigDecimal amount
) {}
