package com.dpbank.api.controller;

import com.dpbank.api.AbstractIntegrationTest;
import com.dpbank.api.domain.Account;
import com.dpbank.api.domain.TransactionType;
import com.dpbank.api.dto.TransactionRequestDTO;
import com.dpbank.api.repository.AccountRepository;
import com.dpbank.api.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AccountControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @AfterEach
    void cleanup() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

        @Test
        @DisplayName("POST /accounts/{id}/transactions should apply debits and credits returning the updated balance")
        void shouldProcessTransactionsViaController() throws Exception {
        Account account = createAccount("ACC-CTRL-001", new BigDecimal("100.00"));

        List<TransactionRequestDTO> transactions = List.of(
            new TransactionRequestDTO(new BigDecimal("50.00"), TransactionType.CREDIT),
            new TransactionRequestDTO(new BigDecimal("25.00"), TransactionType.DEBIT)
        );

        mockMvc.perform(post("/accounts/{id}/transactions", account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactions)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountId").value(account.getId().toString()))
            .andExpect(jsonPath("$.accountNumber").value("ACC-CTRL-001"))
            .andExpect(jsonPath("$.balance").value(125.00));
        }

        @Test
        @DisplayName("POST /accounts/{id}/transactions should return 422 when balance is insufficient")
        void shouldReturn422WhenBalanceIsInsufficient() throws Exception {
        Account account = createAccount("ACC-CTRL-002", new BigDecimal("10.00"));

        List<TransactionRequestDTO> transactions = List.of(
            new TransactionRequestDTO(new BigDecimal("20.00"), TransactionType.DEBIT)
        );

        mockMvc.perform(post("/accounts/{id}/transactions", account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactions)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.title").value("Insufficient balance"));
        }

        @Test
        @DisplayName("GET /accounts/{id}/balance should return the current balance")
        void shouldFetchBalance() throws Exception {
        Account account = createAccount("ACC-CTRL-003", new BigDecimal("321.45"));

        mockMvc.perform(get("/accounts/{id}/balance", account.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.balance").value(321.45));
        }

        @Test
        @DisplayName("GET /accounts/{id}/balance should return 404 for unknown account")
        void shouldReturn404ForUnknownAccount() throws Exception {
        mockMvc.perform(get("/accounts/{id}/balance", UUID.randomUUID()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Resource not found"));
        }

        private Account createAccount(String accountNumber, BigDecimal balance) {
        return accountRepository.save(Account.builder()
            .accountNumber(accountNumber)
            .balance(balance)
            .build());
        }
}
