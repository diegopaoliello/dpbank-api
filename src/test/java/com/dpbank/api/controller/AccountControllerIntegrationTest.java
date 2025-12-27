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
    @DisplayName("POST /accounts/{id}/transactions deve aplicar creditos e debitos e retornar saldo atualizado")
    void deveProcessarLancamentosViaController() throws Exception {
        Account account = criarConta("ACC-CTRL-001", new BigDecimal("100.00"));

        List<TransactionRequestDTO> lancamentos = List.of(
                new TransactionRequestDTO(new BigDecimal("50.00"), TransactionType.CREDITO),
                new TransactionRequestDTO(new BigDecimal("25.00"), TransactionType.DEBITO)
        );

        mockMvc.perform(post("/accounts/{id}/transactions", account.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lancamentos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(account.getId().toString()))
                .andExpect(jsonPath("$.saldo").value(125.00));
    }

    @Test
    @DisplayName("POST /accounts/{id}/transactions deve retornar 422 quando saldo for insuficiente")
    void deveRetornar422QuandoSaldoInsuficiente() throws Exception {
        Account account = criarConta("ACC-CTRL-002", new BigDecimal("10.00"));

        List<TransactionRequestDTO> lancamentos = List.of(
                new TransactionRequestDTO(new BigDecimal("20.00"), TransactionType.DEBITO)
        );

        mockMvc.perform(post("/accounts/{id}/transactions", account.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lancamentos)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Saldo insuficiente"));
    }

    @Test
    @DisplayName("GET /accounts/{id}/balance deve retornar saldo atual")
    void deveConsultarSaldo() throws Exception {
        Account account = criarConta("ACC-CTRL-003", new BigDecimal("321.45"));

        mockMvc.perform(get("/accounts/{id}/balance", account.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.saldo").value(321.45));
    }

    @Test
    @DisplayName("GET /accounts/{id}/balance deve retornar 404 para conta inexistente")
    void deveRetornar404ParaContaInexistente() throws Exception {
        mockMvc.perform(get("/accounts/{id}/balance", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso nao encontrado"));
    }

    private Account criarConta(String numeroConta, BigDecimal saldo) {
        return accountRepository.save(Account.builder()
                .numeroConta(numeroConta)
                .saldo(saldo)
                .build());
    }
}
