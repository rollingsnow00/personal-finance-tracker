package com.finance.tracker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.tracker.model.Role;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {
    private static final String PASSWORD = "FinancePass1!";

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    @Test
    void registrationLoginAndValidationWork() throws Exception {
        String username = newUsername();
        String email = username + "@example.com";
        String registerBody = "{\"username\":\"" + username + "\",\"email\":\"" + email
                + "\",\"password\":\"" + PASSWORD + "\"}";

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.roles[0]").value("USER"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
        org.junit.jupiter.api.Assertions.assertTrue(userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow().getPasswordHash().startsWith("$2a$12$"));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + username + "\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username/email or password"));

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bad\",\"email\":\"bad@example.com\",\"password\":\"weak\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.password").value(containsString("at least 8 characters")));

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody))
                .andExpect(status().isConflict());
    }

    @Test
    void categoriesTransactionsBudgetsSearchReportsAndSoftDeleteWorkTogether() throws Exception {
        String username = newUsername();
        String token = registerAndGetToken(username);
        String adminToken = createAdminToken();

        long expenseCategoryId = createCategory(token, "Meals-" + username, "EXPENSE");
        long incomeCategoryId = createCategory(token, "Salary-" + username, "INCOME");
        mvc.perform(get("/api/categories/{id}", expenseCategoryId).header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.createdBy").value(username))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
        mvc.perform(post("/api/categories").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Meals-" + username + "\",\"type\":\"EXPENSE\"}"))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/categories").header("Authorization", bearer(token)).param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(2));

        LocalDate today = LocalDate.now();
        long expenseId = createTransaction(token, expenseCategoryId, "EXPENSE", "24.50", today, "Market lunch");
        long incomeId = createTransaction(token, incomeCategoryId, "INCOME", "5000.00", today, "Monthly salary");

        mvc.perform(get("/api/transactions").header("Authorization", bearer(token))
                        .param("from", today.toString()).param("to", today.toString())
                        .param("type", "EXPENSE").param("categoryId", String.valueOf(expenseCategoryId))
                        .param("minAmount", "20").param("maxAmount", "30").param("query", "market")
                        .param("page", "0").param("size", "10").param("sortBy", "amount").param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(expenseId));

        mvc.perform(put("/api/transactions/{id}", expenseId).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionBody(expenseCategoryId, "EXPENSE", "25.00", today, "Market and cafe")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(25.00))
                .andExpect(jsonPath("$.lastModifiedBy").value(username));

        String budgetBody = "{\"month\":\"" + YearMonth.from(today) + "\",\"limitAmount\":1000.00,\"currency\":\"PLN\",\"categoryId\":" + expenseCategoryId + "}";
        String budgetResponse = mvc.perform(post("/api/budgets").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content(budgetBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(expenseCategoryId))
                .andReturn().getResponse().getContentAsString();
        long budgetId = objectMapper.readTree(budgetResponse).get("id").asLong();
        mvc.perform(post("/api/budgets").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content(budgetBody))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/budgets/{id}", budgetId).header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.createdBy").value(username));
        mvc.perform(put("/api/budgets/{id}", budgetId).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"month\":\"" + YearMonth.from(today) + "\",\"limitAmount\":1200.00,\"currency\":\"PLN\",\"categoryId\":" + expenseCategoryId + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.limitAmount").value(1200.00));
        mvc.perform(get("/api/budgets").header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));

        mvc.perform(get("/api/reports/monthly").header("Authorization", bearer(token))
                        .param("month", YearMonth.from(today).toString()).param("currency", "PLN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.income").value(5000.00))
                .andExpect(jsonPath("$.expenses").value(25.00))
                .andExpect(jsonPath("$.balance").value(4975.00))
                .andExpect(jsonPath("$.byCategory[0].budgetLimit").value(1200.00));

        mvc.perform(delete("/api/transactions/{id}", expenseId).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/transactions/{id}", expenseId).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/admin/transactions/{id}/restore", expenseId).header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/transactions/{id}/restore", expenseId).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/transactions/{id}", expenseId).header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/budgets/{id}", budgetId).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/budgets/{id}", budgetId).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/admin/budgets/{id}/restore", budgetId).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/budgets/{id}", budgetId).header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/categories/{id}", expenseCategoryId).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/admin/categories/{id}/restore", expenseCategoryId).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());

        mvc.perform(get("/api/transactions/{id}", incomeId).header("Authorization", bearer(newToken(newUsername()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void unauthenticatedRequestsAndInvalidBusinessRulesReturnStructuredErrors() throws Exception {
        mvc.perform(get("/api/categories")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/api/categories"));
        mvc.perform(get("/api/hello")).andExpect(status().isOk());
        mvc.perform(options("/api/categories").header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").isNotEmpty())
                .andExpect(jsonPath("$.paths['/api/transactions'].get.security[0]['Bearer Authentication']").exists())
                .andExpect(jsonPath("$.paths['/api/transactions/{id}'].delete.responses['204']").exists())
                .andExpect(jsonPath("$.paths['/api/auth/register'].post.requestBody.content['application/json'].schema['$ref']").exists());

        String username = newUsername();
        String token = registerAndGetToken(username);
        mvc.perform(get("/api/transactions").header("Authorization", bearer(token))
                        .param("from", "2026-11-30").param("to", "2026-11-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.from").value(containsString("on or before")));

        long incomeCategoryId = createCategory(token, "Income-" + username, "INCOME");
        mvc.perform(post("/api/budgets").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"month\":\"" + YearMonth.now() + "\",\"limitAmount\":100.00,\"currency\":\"PLN\",\"categoryId\":" + incomeCategoryId + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Budgets can only be created for expense categories"));

        mvc.perform(post("/api/transactions").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content(transactionBody(incomeCategoryId, "EXPENSE", "12.00", LocalDate.now(), "Wrong type")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Transaction type must match the category type"));
    }

    private long createCategory(String token, String name, String type) throws Exception {
        String response = mvc.perform(post("/api/categories").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"type\":\"" + type + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private long createTransaction(String token, long categoryId, String type, String amount,
                                  LocalDate date, String description) throws Exception {
        String response = mvc.perform(post("/api/transactions").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionBody(categoryId, type, amount, date, description)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private String transactionBody(long categoryId, String type, String amount, LocalDate date, String description) {
        return "{\"type\":\"" + type + "\",\"amount\":" + amount + ",\"transactionDate\":\"" + date
                + "\",\"description\":\"" + description + "\",\"currency\":\"PLN\",\"categoryId\":" + categoryId + "}";
    }

    private String registerAndGetToken(String username) throws Exception {
        String response = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"email\":\"" + username + "@example.com\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("accessToken").asText();
    }

    private String createAdminToken() {
        String username = newUsername();
        User admin = userRepository.save(new User(username, username + "@example.com",
                passwordEncoder.encode(PASSWORD), Set.of(Role.USER, Role.ADMIN)));
        UserDetails details = org.springframework.security.core.userdetails.User.withUsername(admin.getUsername())
                .password(admin.getPasswordHash()).authorities("ROLE_USER", "ROLE_ADMIN").build();
        return jwtService.issue(details).value();
    }

    private String newToken(String username) {
        User user = userRepository.save(new User(username, username + "@example.com",
                passwordEncoder.encode(PASSWORD), Set.of(Role.USER)));
        UserDetails details = org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPasswordHash()).authorities("ROLE_USER").build();
        return jwtService.issue(details).value();
    }

    private String newUsername() { return "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 14); }
    private String bearer(String token) { return "Bearer " + token; }
}
