package com.example.banking_monolith.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.example.banking_monolith.model.Account;
import com.example.banking_monolith.repository.AccountRepository;
import com.example.banking_monolith.repository.TransactionRepository;

@SpringBootTest
public class OptimisticLockingIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void testConcurrentTransfersCauseOptimisticLockingFailure() throws Exception {
        // Arrange: Create three accounts
        // Source account has plenty of balance
        Account source = new Account("ACC-SRC", "Source Owner", new BigDecimal("100.00"));
        Account dest1 = new Account("ACC-DST1", "Dest 1 Owner", new BigDecimal("0.00"));
        Account dest2 = new Account("ACC-DST2", "Dest 2 Owner", new BigDecimal("0.00"));

        accountRepository.save(source);
        accountRepository.save(dest1);
        accountRepository.save(dest2);

        // Act: Execute two transfers from the same source account concurrently
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        Callable<Void> task1 = () -> {
            transferService.transfer("ACC-SRC", "ACC-DST1", new BigDecimal("10.00"));
            return null;
        };

        Callable<Void> task2 = () -> {
            transferService.transfer("ACC-SRC", "ACC-DST2", new BigDecimal("10.00"));
            return null;
        };

        List<Callable<Void>> tasks = List.of(task1, task2);
        List<Future<Void>> futures = executor.invokeAll(tasks);
        
        int successCount = 0;
        int failureCount = 0;
        Throwable lockingException = null;

        for (Future<Void> future : futures) {
            try {
                future.get();
                successCount++;
            } catch (ExecutionException e) {
                failureCount++;
                lockingException = e.getCause();
            }
        }

        executor.shutdown();

        // Assert: One transfer should succeed, and the other should fail with Optimistic Locking Failure
        assertEquals(1, successCount, "Exactly one concurrent transfer should succeed");
        assertEquals(1, failureCount, "Exactly one concurrent transfer should fail");
        
        assertTrue(lockingException instanceof ObjectOptimisticLockingFailureException 
            || (lockingException != null && lockingException.getMessage().contains("optimistic")),
            "The failure exception must be an ObjectOptimisticLockingFailureException. Got: " + lockingException);

        // Verify that the final balance of the source account is exactly 90.00 (only one transfer subtracted 10.00)
        Account updatedSource = accountRepository.findByAccountNumber("ACC-SRC").orElseThrow();
        assertEquals(new BigDecimal("90.00"), updatedSource.getBalance());
    }
}
