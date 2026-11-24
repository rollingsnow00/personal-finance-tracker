# Personal Finance Tracker API

A Spring Boot REST API for recording personal income and expenses, organizing them by category, setting monthly category budgets, and reviewing monthly totals. Each user's financial records are private to that account. The API is designed to be tested in Postman first and consumed by a Flutter client later.

The main resources are `User`, `Category`, `Transaction`, and `Budget`. The API keeps each amount in its recorded currency and reports totals per currency; it does not convert exchange rates. A later Flutter client can use the JWT bearer token and the configured local CORS origins.

## Technology

- Java 21 and Spring Boot 3.2.12
- Maven Wrapper
- Spring Web, Spring Data JPA, Spring Security, Bean Validation
- PostgreSQL for local runtime; H2 for automated tests
- JWT bearer authentication with BCrypt password hashing
- OpenAPI/Swagger, JUnit 5, Mockito, MockMvc, JaCoCo

## Requirements

- JDK 21
- PostgreSQL 17 (or a compatible PostgreSQL version)
- Postman for the manual API flow
- No global Maven installation is needed; the wrapper downloads the pinned Maven version.

## Local setup

1. Create the database role and database. For example, from `psql -U postgres`:

   ```sql
   CREATE USER finance_app WITH PASSWORD 'your-local-db-password';
   CREATE DATABASE personal_finance_tracker OWNER finance_app;
   ```

2. Copy `.local-dev.env.example` to `.local-dev.env` in this project directory. Set `DB_PASSWORD` to the same role password, and choose strong local-only demo passwords. Generate `JWT_SECRET` in PowerShell with a cryptographic random key:

   ```powershell
   $bytes = New-Object byte[] 32
   [Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
   [Convert]::ToBase64String($bytes)
   ```

   Paste the result as `JWT_SECRET`. Keep `.local-dev.env` private; it is ignored by Git.

3. Run the app from PowerShell in the project directory:

   ```powershell
   .\run-local.ps1
   ```

   The script loads `.local-dev.env`, activates the `local` profile, and starts the API on `http://localhost:8080`. With `DEMO_DATA_ENABLED=true`, it creates an admin account (`admin@example.com`, roles USER and ADMIN), a regular account (`demo@example.com`), and starter categories. Their passwords are the values you set in `.local-dev.env`.

4. Open Swagger UI at `http://localhost:8080/swagger-ui.html`. The Postman collection is in `postman/Personal Finance Tracker.postman_collection.json`; import it with `postman/Personal Finance Tracker.postman_environment.json` and enter the demo passwords in the environment.

The schema uses Hibernate `ddl-auto=update` for this coursework project. Do not use the local demo profile or demo accounts for real financial data.

## Run tests and coverage

```powershell
.\test.ps1
```

The H2 test profile avoids requiring a running PostgreSQL instance. JaCoCo writes its HTML report to `target/site/jacoco/index.html`.

## API overview

| Area | Endpoints |
|---|---|
| Authentication | `POST /api/auth/register`, `POST /api/auth/login` |
| Categories | `GET/POST /api/categories`, `GET/PUT/DELETE /api/categories/{id}` |
| Transactions | `GET/POST /api/transactions`, `GET/PUT/DELETE /api/transactions/{id}` |
| Budgets | `GET/POST /api/budgets`, `GET/PUT/DELETE /api/budgets/{id}` |
| Reports | `GET /api/reports/monthly?month=2026-11&currency=PLN` |
| Admin restore | `POST /api/admin/categories/{id}/restore`, `/api/admin/transactions/{id}/restore`, `/api/admin/budgets/{id}/restore` |
| Lab 1 REST basics | `GET /api/hello`, `GET /api/hello/{name}`, `POST /api/hello` |

Protected endpoints use `Authorization: Bearer <accessToken>`. List endpoints return Spring `Page` metadata and accept `page`, `size`, `sortBy`, and `direction`. Transaction filters include `from`, `to`, `type`, `categoryId`, `minAmount`, `maxAmount`, and `query`. Page size is limited to 100. DELETE records a deletion timestamp; only ADMIN can restore a deleted record.

## Project structure

`controller` handles HTTP, `service` contains business rules, `repository` handles persistence, `model` contains JPA entities, `dto` defines API contracts, `mapper` converts entities to responses, `security` implements JWT access, and `exception` provides consistent API errors.

## Features

### Mandatory course features

- Registration and login with BCrypt password hashes and JWT authentication
- USER and ADMIN roles; admin-only restore endpoints
- Full CRUD for categories, transactions, and budgets with user ownership and entity relationships
- Request/response DTOs, Bean Validation, and custom password/date-range validators
- Global structured error handling and PostgreSQL persistence
- OpenAPI/Swagger documentation and service/integration tests

### Additional features

- User-owned income, expense, category, and monthly budget CRUD
- Monthly totals and category spending report, grouped by currency
- Pagination and sorting; multi-criteria search with date range filtering
- Soft delete with ADMIN restore
- Audit timestamps and user attribution

