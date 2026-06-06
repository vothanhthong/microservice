package com.example.banking_monolith.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.banking_monolith.model.IdempotentRequest;

public interface IdempotentRequestRepository extends JpaRepository<IdempotentRequest, UUID> {
}
