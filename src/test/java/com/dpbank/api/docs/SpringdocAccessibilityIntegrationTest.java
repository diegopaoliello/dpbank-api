package com.dpbank.api.docs;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SpringdocAccessibilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Swagger UI deve redirecionar corretamente a partir de /docs")
    void swaggerUiIsReachable() throws Exception {
        mockMvc.perform(get("/docs"))
            .andExpect(status().is3xxRedirection())
            .andExpect(result -> {
                String location = result.getResponse().getHeader("Location");
                if (location == null || !location.contains("swagger-ui")) {
                    throw new AssertionError("Redirecionamento inesperado: " + location);
                }
            });
    }

    @Test
    @DisplayName("Contrato OpenAPI deve ser retornado em /docs/api")
    void openApiJsonIsReachable() throws Exception {
        mockMvc.perform(get("/docs/api"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.openapi").exists())
            .andExpect(jsonPath("$.info.title").exists());
    }
}
