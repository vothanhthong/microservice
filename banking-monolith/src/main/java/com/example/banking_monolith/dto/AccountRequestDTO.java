package com.example.banking_monolith.dto;

import java.math.BigDecimal;

/**
 * A modern Java Record representing the payload to create a bank account.
 * Records are immutable data carriers, perfect for DTOs.
 */
public record AccountRequestDTO(
    String ownerName,
    BigDecimal initialBalance
) {}
