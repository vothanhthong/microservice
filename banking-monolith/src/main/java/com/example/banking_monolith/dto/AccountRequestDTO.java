package com.example.banking_monolith.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * A modern Java Record representing the payload to create a bank account.
 * Records are immutable data carriers, perfect for DTOs.
 */
public record AccountRequestDTO(
    @NotBlank(message = "Owner name must not be blank")
    String ownerName,

    @NotNull(message = "Initial balance must not be null")
    @PositiveOrZero(message = "Initial balance must be zero or positive")
    BigDecimal initialBalance
) {}
