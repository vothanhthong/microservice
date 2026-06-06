# Learning Plan: Spring Boot Monolith to Microservices

Welcome to your banking learning project! This document outlines our roadmap to go from a simple Spring Boot Monolith to a Microservices architecture.

---

## 🎯 Project Goals
1. Learn core Spring Boot concepts (Controllers, Services, JPA, Repositories).
2. Learn database modeling using an in-memory SQL database (H2).
3. Verify APIs using Bruno/curl.
4. Understand how to break apart a monolith into separate microservices.
5. Apply enterprise-grade patterns (concurrency control, input validation, idempotency, distributed transactions).

---

## 🛠️ Tech Stack (Monolith Phase)
* **Framework:** Spring Boot 3.x
* **Language:** Java 17+
* **Build Tool:** Maven
* **Database:** H2 (In-memory SQL)
* **ORM:** Spring Data JPA (Hibernate)

---

## 📐 Git Workflow & Coding Conventions

### 1. Git Branching Strategy
We will use feature branches to keep our `main` branch clean. For each step:
* **Checkout a new branch:** `git checkout -b feature/<branch-name>` from `main`.
* **Merge back:** After testing and verification, merge into `main` using `git merge feature/<branch-name>`.

### 2. Conventional Commits
Use prefix-based commit messages to keep a clear log:
* `feat: ...` for new features
* `fix: ...` for bug fixes
* `docs: ...` for documentation changes
* `refactor: ...` for code refactoring
* `test: ...` for adding tests
* `chore: ...` for build dependencies/chores

---

## 🗺️ Roadmap Checklist

### Phase 1: The Monolith (Completed! 🎉)
* [x] **Step 1: Project Initialization**
* [x] **Step 2: Database Configuration**
* [x] **Step 3: Define the Models & Entities**
* [x] **Step 4: Create the JPA Repositories**
* [x] **Step 5: Implement Business Services**
* [x] **Step 6: Build REST Controllers (APIs)**
* [x] **Step 7: Testing with Bruno**

---

### Phase 1.5: Monolith Enterprise Upgrades (Next Up 🚀)

#### [ ] Step 1: Data Defense & Input Validation
* [ ] Create branch `feature/monolith-validation`.
* [ ] Add `spring-boot-starter-validation` dependency to `pom.xml`.
* [ ] Add Bean Validation annotations to DTOs:
  - `AccountRequestDTO`: `@NotBlank` for `ownerName`, `@NotNull` & `@PositiveOrZero` for `initialBalance`.
  - `TransferRequestDTO`: `@NotBlank` for account numbers, `@NotNull` & `@Positive` for `amount`.
* [ ] Add `@Valid` to controller methods.
* [ ] Add `MethodArgumentNotValidException` handler in `GlobalExceptionHandler` to return specific field error messages with a `400 Bad Request` status.
* [ ] Test with Bruno validation failure payloads and verify clean JSON error maps.
* [ ] Merge branch into `main`.

#### [ ] Step 2: Concurrency & Race Conditions (Optimistic Locking)
* [ ] Create branch `feature/monolith-concurrency`.
* [ ] Add a `@Version` field (`private Long version;`) to the `Account` entity.
* [ ] Add `ObjectOptimisticLockingFailureException` handler in `GlobalExceptionHandler` to return a `409 Conflict` status.
* [ ] Write an integration test to simulate two concurrent transfers running at the same time and verify one is rejected with 409 Conflict.
* [ ] Merge branch into `main`.

#### [ ] Step 3: API Idempotency
* [ ] Create branch `feature/monolith-idempotency`.
* [ ] Create an `IdempotentRequest` entity to store: `idempotencyKey` (UUID, Primary Key), `responseBody` (String), `statusCode` (int), and `createdAt` (LocalDateTime).
* [ ] Create `IdempotentRequestRepository`.
* [ ] Implement an Idempotency Interceptor/Filter or Service check that intercepts `POST /api/transfers`:
  - Requires `Idempotency-Key` HTTP Header (rejects with 400 if missing or invalid UUID format).
  - If key is new: processes the transfer, caches the response payload in `IdempotentRequest`, and returns the result.
  - If key exists: bypasses transfer logic, fetches the cached response, and returns it.
* [ ] Write Bruno test requests using the same `Idempotency-Key` header twice to verify the transfer runs only once but returns the identical response.
* [ ] Merge branch into `main`.

---

### Phase 2: Refactoring to Microservices (Revised 🌐)

#### [ ] Step 1: Microservice Boundary Planning & Port Setup
* [ ] Design boundaries for `account-service` and `transaction-service`.
* [ ] Set up isolated H2 databases (`accountdb` and `transactiondb`).

#### [ ] Step 2: Extracting Account Service
* [ ] Build standalone `account-service` on port `8081`.
* [ ] Migrate validation, optimistic locking, and H2 console settings.

#### [ ] Step 3: Extracting Transaction Service
* [ ] Build standalone `transaction-service` on port `8082`.
* [ ] Migrate transaction logs and H2 console.

#### [ ] Step 4: Distributed Consistency (Saga Choreography Pattern)
* [ ] Integrate message broker (e.g. RabbitMQ/Kafka) using Spring Cloud Stream or simulate it locally.
* [ ] Implement **Saga Choreography**:
  1. `transaction-service` logs a pending transfer event (`TransferRequestedEvent`).
  2. `account-service` consumes it, performs balance checks/deductions, and emits `FundsDeductedEvent` (success) or `DeductionFailedEvent` (failure).
  3. `transaction-service` consumes the result to finalize the transaction or run compensating rollbacks.
* [ ] Implement compensating rollback flows if transaction fails.

#### [ ] Step 5: Distributed Idempotency & Validation Testing
* [ ] Verify end-to-end idempotency over HTTP routing.
* [ ] Test with Bruno collections against ports `8081` and `8082`.
