package br.org.edu.ifrn.LojaCarro.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect") // Adicionado o dialeto correto aqui
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.ANY)
@Sql(scripts = "/test-data-carros.sql")
@DisplayName("Integration tests for CarroController with DB seeded by @Sql")
public class CarroControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("GET /carro should return 10 carros inserted by @Sql")
	void testListAllCarsSqlInserted() throws Exception {
		mockMvc.perform(get("/carro"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(10));
	}

	@Test
	@DisplayName("GET /carro/{id} - deve retornar 404 para ID inexistente")
	void testGetCarroIdInexistente() throws Exception {
		mockMvc.perform(get("/carro/999"))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("POST /carro/salvar - deve retornar 400 para preço negativo")
	@WithMockUser(username = "testuser", roles = {"VENDEDOR"})
	void testPostCarroPrecoNegativo() throws Exception {
		// Criando o JSON na mão para evitar dependências de conversão externa
		String carroJson = "{\"modelo\":\"Civic\",\"ano\":2023,\"preco\":-50.0}";

		mockMvc.perform(post("/carro/salvar")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.content(carroJson))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.preco").value(org.hamcrest.Matchers.containsString("Preço deve ser positivo")));
	}
}

