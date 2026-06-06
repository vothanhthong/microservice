package com.example.banking_monolith.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.banking_monolith.model.Account;
import com.example.banking_monolith.model.Transaction;
import com.example.banking_monolith.repository.AccountRepository;
import com.example.banking_monolith.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransferService transferService;

    @Test
    void transfer_Success() {
        // Arrange
        String sourceAccNum = "ACC-1001";
        String destAccNum = "ACC-1002";
        
        Account sourceAccount = new Account(sourceAccNum, "Alice", new BigDecimal("100.00"));
        Account destAccount = new Account(destAccNum, "Bob", new BigDecimal("50.00"));

        when(accountRepository.findByAccountNumber(sourceAccNum)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber(destAccNum)).thenReturn(Optional.of(destAccount));
        
        // Mock transactionRepository.save to return the passed transaction object
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Transaction transaction = transferService.transfer(sourceAccNum, destAccNum, new BigDecimal("30.00"));

        // Assert
        assertNotNull(transaction);
        assertEquals(sourceAccNum, transaction.getSourceAccountNumber());
        assertEquals(destAccNum, transaction.getDestinationAccountNumber());
        assertEquals(new BigDecimal("30.00"), transaction.getAmount());

        // Verify balances updated in memory
        assertEquals(new BigDecimal("70.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("80.00"), destAccount.getBalance());

        // Verify DB interactions
        verify(accountRepository, times(1)).save(sourceAccount);
        verify(accountRepository, times(1)).save(destAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void transfer_InsufficientBalance_ThrowsException() {
        // Arrange
        String sourceAccNum = "ACC-1001";
        String destAccNum = "ACC-1002";
        
        Account sourceAccount = new Account(sourceAccNum, "Alice", new BigDecimal("20.00"));
        Account destAccount = new Account(destAccNum, "Bob", new BigDecimal("50.00"));

        when(accountRepository.findByAccountNumber(sourceAccNum)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber(destAccNum)).thenReturn(Optional.of(destAccount));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transferService.transfer(sourceAccNum, destAccNum, new BigDecimal("30.00"));
        });

        assertEquals("Insufficient balance in account: " + sourceAccNum, exception.getMessage());

        // Verify we never saved anything or logged a transaction
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
