package com.example.banking_monolith.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.example.banking_monolith.model.IdempotentRequest;
import com.example.banking_monolith.repository.IdempotentRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotentRequestRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * Retrieves a cached request if it exists.
     */
    public Optional<IdempotentRequest> getCachedRequest(UUID idempotencyKey) {
        return repository.findById(idempotencyKey);
    }

    /**
     * Deserializes the response body JSON back into the original object type.
     */
    public <T> T deserializeResponse(String json, Class<T> responseType) {
        try {
            return objectMapper.readValue(json, responseType);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to deserialize response for idempotency", e);
        }
    }

    /**
     * Serializes the response and saves it as an idempotent cache entry.
     */
    @Transactional
    public void saveResponse(UUID idempotencyKey, Object response, int statusCode) {
        try {
            String json = objectMapper.writeValueAsString(response);
            IdempotentRequest record = new IdempotentRequest(
                idempotencyKey,
                json,
                statusCode,
                LocalDateTime.now()
            );
            repository.save(record);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize response for idempotency", e);
        }
    }
}
