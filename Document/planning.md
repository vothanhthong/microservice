# Learning Plan: Spring Boot Monolith to Microservices

Welcome to your banking learning project! This document outlines our roadmap to go from a simple Spring Boot Monolith to a Microservices architecture.

---

## 🎯 Project Goals
1. Learn core Spring Boot concepts (Controllers, Services, JPA, Repositories).
2. Learn database modeling using an in-memory SQL database (H2).
3. Verify APIs using Bruno/curl.
4. Understand how to break apart a monolith into separate microservices.

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
* **Branch Names:**
  - Step 1 & 2: `feature/monolith-setup`
  - Step 3 & 4: `feature/monolith-data-model`
  - Step 5: `feature/monolith-services`
  - Step 6 & 7: `feature/monolith-apis`

### 2. Conventional Commits
Use prefix-based commit messages to keep a clear log:
* `feat: ...` for new features (e.g., `feat: add Account controller endpoints`)
* `fix: ...` for bug fixes (e.g., `fix: resolve balance validation error`)
* `docs: ...` for documentation changes (e.g., `docs: update setup instructions`)
* `refactor: ...` for code refactoring (e.g., `refactor: extract transaction verification logic`)

### 3. Java Coding Conventions
* **Package Naming:** All lowercase (e.g., `com.example.banking.controller`).
* **Class Names:** PascalCase (e.g., `AccountController`, `TransferService`).
* **Method & Variable Names:** camelCase (e.g., `getAccountBalance`, `accountNumber`).
* **Database Entities:** Plain Java objects (POJOs) with `@Entity` annotation. Use standard Getters/Setters.
* **DTOs (Data Transfer Objects):** Use Java `record`s for read-only request/response structures (available in Java 16+).
* **Dependency Injection:** Use Constructor Injection for Spring beans instead of `@Autowired` field injection. (Standard industry best practice).

---

## 🗺️ Roadmap Checklist

### Phase 1: The Monolith (We are here 📍)

#### [x] Step 1: Project Initialization
* [x] Create branch `feature/monolith-setup` (`git checkout -b feature/monolith-setup`).
* [x] Generate the project skeleton on [Spring Initializr](https://start.spring.io) (Maven, Java 17+, Group: `com.example`, Artifact: `banking-monolith`, dependencies: Spring Web, Spring Data JPA, H2).
* [x] Download and extract the code into the workspace.
* [x] Verify Java compiles by running `./mvnw compile` in the terminal.
* [x] Commit files: `feat: initialize spring boot project structure`.

#### [x] Step 2: Database Configuration
* [x] Configure H2 in `src/main/resources/application.properties` (set in-memory URL, enable H2 console path, and set username/password).
* [x] Run the app using `./mvnw spring-boot:run` and verify H2 console is accessible at `http://localhost:8080/h2-console`.
* [x] Commit changes: `feat: configure H2 database settings`.

#### [x] Step 3: Define the Models & Entities
* [x] Create branch `feature/monolith-data-model` (`git checkout -b feature/monolith-data-model`).
* [x] Create `com.example.banking.model.Account` with properties: `id` (Long, `@Id`), `accountNumber` (String, unique), `ownerName` (String), and `balance` (BigDecimal).
* [x] Create `com.example.banking.model.Transaction` with properties: `id` (Long, `@Id`), `sourceAccountNumber` (String), `destinationAccountNumber` (String), `amount` (BigDecimal), and `timestamp` (LocalDateTime).
* [x] Commit models: `feat: add Account and Transaction entity models`.

#### [x] Step 4: Create the JPA Repositories
* [x] Create `com.example.banking.repository.AccountRepository` extending `JpaRepository<Account, Long>`. Add a custom method `Optional<Account> findByAccountNumber(String accountNumber)`.
* [x] Create `com.example.banking.repository.TransactionRepository` extending `JpaRepository<Transaction, Long>`. Add separate queries for source and destination accounts to keep naming simple.
* [x] Commit repositories: `feat: create account and transaction JPA repositories`.
* [x] Merge branch to main: Switch to main (`git checkout main`), merge (`git merge feature/monolith-data-model`), and delete feature branch.

#### [ ] Step 5: Implement Business Services
* [x] Create branch `feature/monolith-services` (`git checkout -b feature/monolith-services`).
* [x] Create `com.example.banking.service.AccountService` with methods: `createAccount()`, `getAccountByNumber()`, and helper update balance logic.
* [x] Create `com.example.banking.service.TransferService` containing business logic for:
  - Checking if both accounts exist.
  - Ensuring the sender has sufficient balance.
  - Making balance updates atomic (use `@Transactional` annotation).
  - Saving a `Transaction` ledger log.
* [x] Write simple JUnit unit tests for transfer scenarios (successful transfer, insufficient balance).
* [ ] Commit services: `feat: implement AccountService and TransferService business logic`.
* [ ] Merge branch to main.

#### [ ] Step 6: Build REST Controllers (APIs)
* [ ] Create branch `feature/monolith-apis` (`git checkout -b feature/monolith-apis`).
* [ ] Create `com.example.banking.controller.AccountController` exposing:
  - `POST /api/accounts` (using an `AccountRequestDTO` record).
  - `GET /api/accounts/{accountNumber}`.
* [ ] Create `com.example.banking.controller.TransferController` exposing:
  - `POST /api/transfers` (using a `TransferRequestDTO` record).
  - `GET /api/transfers/history/{accountNumber}` (view history).
* [ ] Add basic global error handling using `@RestControllerAdvice` to map exceptions to clean HTTP responses (e.g. 400 Bad Request if balance is insufficient).
* [ ] Commit APIs: `feat: build REST endpoints and controller advice exception handler`.

#### [ ] Step 7: Testing with Bruno
* [ ] Create a `bruno/` directory in the root.
* [ ] Setup Bruno collections and create requests to test:
  - Create account A (with balance 100).
  - Create account B (with balance 50).
  - Get Account A details.
  - Transfer 30 from A to B.
  - Verify Account A balance is 70 and B is 80.
  - Get transaction history for A.
* [ ] Commit collections: `test: add bruno API validation requests`.
* [ ] Merge branch to main.

---

### Phase 2: Refactoring to Microservices (Coming Later 🚀)

#### [ ] Step 1: Microservice Boundary Planning
* [ ] Separate monolith logic into `Account Service` and `Transaction Service`.
* [ ] Design isolated databases for each service.

#### [ ] Step 2: Extracting Account Service
* [ ] Create a new Spring Boot project `account-service`.
* [ ] Extract `Account` model, repository, service, and controller.
* [ ] Run and test independently on port `8081`.

#### [ ] Step 3: Extracting Transaction Service
* [ ] Create a new Spring Boot project `transaction-service`.
* [ ] Extract `Transaction` model, repository, and controller.
* [ ] Configure to run on port `8082`.

#### [ ] Step 4: Inter-Service HTTP Communication
* [ ] Configure `RestTemplate` or `WebClient` in the `Transaction Service` to query and adjust balances in the `Account Service` over HTTP.
* [ ] Implement validation check checks across service boundaries.

#### [ ] Step 5: Distributed Consistency & Testing
* [ ] Discuss handling edge cases: What happens if service A goes down mid-transaction?
* [ ] Update Bruno collections to test against both microservices.
