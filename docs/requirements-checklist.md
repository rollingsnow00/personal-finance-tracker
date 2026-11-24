# Assignment requirements checklist

This checklist maps the numbered PDFs to the project. Optional items are marked separately. The instructor still has to approve the topic in Lab 1; that approval cannot be recorded by this code repository.

## Lab 1 - Project setup and REST basics

- [x] Topic documented: Personal Finance Tracker API; entities: User, Category, Transaction, Budget.
- [x] Spring Boot 3.2.x, Java 17+, Maven, required dependencies, and package structure.
- [x] Application configuration and a basic public REST controller.
- [x] Domain CRUD endpoints and a Postman collection.
- [x] Local Git repository, `.gitignore`, and README.
- [x] Initial project commit.

## Lab 2 - Database integration and JPA

- [x] PostgreSQL configuration; H2 retained for tests.
- [x] At least three JPA entities with IDs, constraints, and created/updated timestamps.
- [x] Entity relationships and repository interfaces.
- [x] CRUD backed by PostgreSQL.
- [x] Pagination support.
- [x] Optional: development data seeder.

## Lab 3 - Service layer and business logic

- [x] Controller -> service -> repository layering with interfaces and implementations.
- [x] Business rules, custom exceptions, consistent exception handling, and useful logging.
- [x] Service unit tests; service coverage goal at least 70%.

## Lab 4 - DTOs, validation, and error handling

- [x] Request and response DTOs for API operations; entities are not returned directly.
- [x] Bean Validation plus custom password/date-range validation.
- [x] Central error responses include validation details and suitable HTTP status codes.
- [x] OpenAPI/Swagger documents the endpoints and bearer-token requirement.
- [x] Validation tests.

## Lab 5 - Authentication and authorization

- [x] Registration and login; passwords hashed with BCrypt.
- [x] JWT generation and validation; protected endpoints.
- [x] USER and ADMIN roles, role-specific restore endpoints, and authorization tests.
- [x] Authentication error responses.
- [ ] refresh token. Maybe we add later.

## Lab 6 - Polishing and additional features

- [x] Pagination and sorting.
- [x] Multi-criteria transaction search and date filtering.
- [x] Soft delete, default hidden records, and ADMIN restore.
- [x] Audit attribution and timestamps (selected extra feature).
- [x] Unit tests and critical endpoint integration tests.
- [x] Service coverage at least 70%; overall coverage goal at least 60%.
- [x] Swagger, README, and code review.
- [x] At least three additional features implemented.

## Final project requirements and defense

- [x] PostgreSQL-backed normalized schema, constraints, and useful indexes.
- [x] CORS configured for local web clients; secrets read from local environment.
- [x] Postman collection, setup instructions, sample accounts, tests, and API docs.
- [x] Defense demo plan and architecture walkthrough notes.
