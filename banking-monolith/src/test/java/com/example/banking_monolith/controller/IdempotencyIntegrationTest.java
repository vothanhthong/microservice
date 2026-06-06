package com.example.banking_monolith.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import com.example.banking_monolith.dto.TransferRequestDTO;
import com.example.banking_monolith.model.Account;
import com.example.banking_monolith.repository.AccountRepository;
import com.example.banking_monolith.repository.IdempotentRequestRepository;
import com.example.banking_monolith.repository.TransactionRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class IdempotencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private IdempotentRequestRepository idempotentRequestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        idempotentRequestRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void testMissingIdempotencyKeyHeader_ReturnsBadRequest() throws Exception {
        TransferRequestDTO request = new TransferRequestDTO("ACC-SRC", "ACC-DST", new BigDecimal("10.00"));
        
        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value(containsString("Idempotency-Key header is required")));
    }

    @Test
    void testInvalidIdempotencyKeyFormat_ReturnsBadRequest() throws Exception {
        TransferRequestDTO request = new TransferRequestDTO("ACC-SRC", "ACC-DST", new BigDecimal("10.00"));
        
        mockMvc.perform(post("/api/transfers")
                .header("Idempotency-Key", "not-a-valid-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value(containsString("must be a valid UUID")));
    }

    @Test
    void testIdempotentRequests_ExecutesOnlyOnce_ReturnsCachedResponseOnDuplicate() throws Exception {
        // Arrange
        Account source = new Account("ACC-SRC", "Source", new BigDecimal("100.00"));
        Account dest = new Account("ACC-DST", "Destination", new BigDecimal("50.00"));
        accountRepository.save(source);
        accountRepository.save(dest);

        TransferRequestDTO request = new TransferRequestDTO("ACC-SRC", "ACC-DST", new BigDecimal("30.00"));
        String key = UUID.randomUUID().toString();

        // Act & Assert: First request (should succeed)
        mockMvc.perform(post("/api/transfers")
                .header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceAccountNumber").value("ACC-SRC"))
                .andExpect(jsonPath("$.destinationAccountNumber").value("ACC-DST"))
                .andExpect(jsonPath("$.amount").value(30.0));

        // Verify balances after first transfer
        Account sourceAfterFirst = accountRepository.findByAccountNumber("ACC-SRC").orElseThrow();
        Account destAfterFirst = accountRepository.findByAccountNumber("ACC-DST").orElseThrow();
        assertEquals(new BigDecimal("70.00"), sourceAfterFirst.getBalance());
        assertEquals(new BigDecimal("80.00"), destAfterFirst.getBalance());
        assertEquals(1, transactionRepository.count(), "Should have exactly 1 transaction in ledger");

        // Act & Assert: Duplicate request with same Idempotency-Key
        mockMvc.perform(post("/api/transfers")
                .header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceAccountNumber").value("ACC-SRC"))
                .andExpect(jsonPath("$.destinationAccountNumber").value("ACC-DST"))
                .andExpect(jsonPath("$.amount").value(30.0));

        // Verify balances did NOT change again
        Account sourceAfterSecond = accountRepository.findByAccountNumber("ACC-SRC").orElseThrow();
        Account destAfterSecond = accountRepository.findByAccountNumber("ACC-DST").orElseThrow();
        assertEquals(new BigDecimal("70.00"), sourceAfterSecond.getBalance(), "Balance should not deduct twice");
        assertEquals(new BigDecimal("80.00"), destAfterSecond.getBalance(), "Balance should not credit twice");
        assertEquals(1, transactionRepository.count(), "Transaction ledger count should still be exactly 1");
    }
}
