# Lab 7 defense notes

## 10-15 minute feature demo

1. Open Swagger UI or the Postman collection and show the public Lab 1 hello endpoints.
2. Log in as `demo@example.com`, explain that the response contains a short-lived bearer token, and call the protected categories endpoint.
3. Create an expense category, transaction, and monthly budget. Show a validation error by sending an invalid amount or a category whose type does not match the transaction.
4. Search transactions by a date range, category, type, amount, and text; show page metadata and sorting.
5. Open the monthly report and explain that totals are grouped by the requested currency.
6. Soft-delete a transaction and show that normal reads no longer return it. Log in as the local admin and restore it.
7. Show the audit timestamps and user attribution on a response, then demonstrate an unauthenticated 401 and a regular-user 403 on an admin endpoint.

The local demo passwords are generated for this machine and live only in the ignored `.local-dev.env` file. Fill the matching secret fields in the Postman environment before the demo.

## Architecture walkthrough

- Controllers translate HTTP requests and responses; they do not access repositories directly.
- Services enforce ownership, category/type compatibility, budget uniqueness, soft-delete rules, and report calculations.
- Repositories use Spring Data JPA; transaction search builds a criteria query and fetches its category to avoid a per-row lookup.
- Request and response DTOs keep JPA entities and password hashes out of the API contract.
- Spring Security validates stateless JWT bearer tokens; BCrypt stores password hashes. Registration only grants USER. Local seed data gives the demo administrator USER and ADMIN.
- PostgreSQL persists the application data. H2 provides an isolated test database. `ddl-auto=update` keeps the coursework schema convenient during development.

## Questions to rehearse

- **Why DTOs?** They validate input, avoid exposing persistence details, and keep password hashes out of responses.
- **How is one user's data separated?** The authenticated username is resolved to a user ID; list, search, read, update, and delete queries are scoped to that ID.
- **Why soft delete?** It preserves finance history and supports the ADMIN restore workflow required by the lab.
- **Why use BCrypt and JWT?** BCrypt hashes passwords; JWT lets a client call the stateless API with a bearer token.
- **How are budgets connected to transactions?** Both point to the user's expense category; the monthly report sums expenses by category and compares them with the matching budget.
- **What do the tests cover?** Registration/login, validation, 401/403/404 behavior, user ownership, CRUD, filtering, reporting, and restore flows. JaCoCo checks the course coverage thresholds during `verify`.
- **What would be needed before production?** Migrations instead of `ddl-auto=update`, deployment-specific CORS origins, key rotation and refresh-token policy, and operational monitoring.
