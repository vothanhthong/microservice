# API Testing Guide with Bruno

This collection contains HTTP requests to test and validate our Spring Boot Banking Monolith API.

---

## 🚀 How to Setup and Run

### 1. Start the Spring Boot Server
Ensure your backend application is running. In the `banking-monolith` directory:
```bash
./mvnw spring-boot:run
```
The server will start on port `8080`.

### 2. Import the Collection in Bruno
1. Open the **Bruno** desktop application.
2. Click **Open Collection** on the home screen.
3. Select the `bruno/` directory in the root of this project.

---

## 🏃‍♂️ Step-by-Step Test Scenarios

### Step 1: Create Account A (Alice)
*   **Request:** `Create Account A` (POST)
*   **Action:** Click **Send**.
*   **Result:** A `201 Created` status is returned. 
*   **Action Required:** Copy the generated `"accountNumber"` (e.g., `ACC-F2A1`) from the JSON response. You will need it for the next steps!

### Step 2: Create Account B (Bob)
*   **Request:** `Create Account B` (POST)
*   **Action:** Click **Send**.
*   **Result:** A `201 Created` status is returned.
*   **Action Required:** Copy Bob's generated `"accountNumber"` (e.g., `ACC-B4C3`).

### Step 3: Fetch Account Details (Get Account A)
*   **Request:** `Get Account` (GET)
*   **Action:** Replace `REPLACE_WITH_ACCOUNT_NUMBER` in the URL path with Alice's actual account number, then click **Send**.
*   **Result:** Verification of Alice's balance (should be exactly `100.00`).

### Step 4: Transfer Funds (A to B)
*   **Request:** `Transfer A to B` (POST)
*   **Action:** In the JSON request body, replace the placeholders with Alice's and Bob's actual account numbers. Click **Send**.
*   **Result:** A `200 OK` status with the transaction log (timestamps, amount, source, and destination).

### Step 5: Verify Updated Balances
*   **Action:** Run `Get Account` again for Alice (balance should have decreased to `70.00`) and Bob (balance should have increased to `80.00`).

### Step 6: Fetch Transaction History
*   **Request:** `Get History A` (GET)
*   **Action:** Replace `REPLACE_WITH_ACC_A` in the URL path with Alice's account number, then click **Send**.
*   **Result:** You will see a list of transactions where Alice's account is involved, sorted from newest to oldest.

---

## ⚠️ Error Validation Test (Bonus)

To verify our global exception handler works correctly:
1. Open the `Transfer A to B` request.
2. Change the transfer amount in the JSON body to `1000.00` (which is higher than Alice's current `70.00` balance).
3. Click **Send**.
4. **Verification:** You should receive a **`400 Bad Request`** status containing our structured error:
   ```json
   {
     "error": "BAD_REQUEST",
     "message": "Insufficient balance in account: ACC-...",
     "timestamp": "..."
   }
   ```
