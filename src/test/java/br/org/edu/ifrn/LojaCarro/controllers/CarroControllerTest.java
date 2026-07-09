package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.CarroException;
import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.security.JwtTokenProvider;
import br.org.edu.ifrn.LojaCarro.security.SecurityConfig;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarroController.class)
@Import(SecurityConfig.class)
@DisplayName("Testes do CarroController")
public class CarroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarroService carroService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /carro/salvar - sucesso")
    @WithMockUser(username = "testuser", roles = {"VENDEDOR"})
    void testSalvarCarroSucesso() throws Exception {
        Carro carro = new Carro("Civic", 2023);
        carro.setId(1L);

        when(carroService.save(any(Carro.class))).thenReturn(carro);

        mockMvc.perform(post("/carro/salvar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(carro)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Civic"))
                .andExpect(jsonPath("$.ano").value(2023));

        verify(carroService, times(1)).save(any(Carro.class));
    }

    @Test
    @DisplayName("POST /carro/salvar - modelo inválido (service lança CarroException)")
    @WithMockUser(username = "testuser", roles = {"VENDEDOR"})
    void testSalvarCarroModeloInvalido() throws Exception {
        Carro carro = new Carro("ModeloMuitoGrande", 2023);

        when(carroService.save(any(Carro.class))).thenThrow(new CarroException("O modelo do carro deve ter menos de 10 caracteres."));

        mockMvc.perform(post("/carro/salvar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(carro)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(org.hamcrest.Matchers.containsString("O modelo do carro deve ter menos de 10 caracteres.")));

        verify(carroService, times(1)).save(any(Carro.class));
    }

    @Test
    @DisplayName("POST /carro/salvar - preco negativo")
    @WithMockUser(username = "testuser", roles = {"VENDEDOR"})
    void testSalvarCarroPrecoNegativo() throws Exception {
        Carro carro = new Carro("Civic", 2023);
        carro.setPreco(-1000.0);

        mockMvc.perform(post("/carro/salvar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(carro)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.preco").value(org.hamcrest.Matchers.containsString("Preço deve ser positivo")));
    }

    @Test
    @DisplayName("PUT /carro/{id} - atualizar carro inexistente")
    @WithMockUser(username = "testuser", roles = {"GERENTE"})
    void testAtualizarCarroInexistenteController() throws Exception {
        Carro c = new Carro("Civic", 2024);
        c.setId(999L);

        when(carroService.update(any(Carro.class))).thenThrow(new CarroException("Carro com ID 999 não encontrado para atualização."));

        mockMvc.perform(put("/carro/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(c)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(org.hamcrest.Matchers.containsString("não encontrado para atualização")));

        verify(carroService, times(1)).update(any(Carro.class));
    }

    @Test
    @DisplayName("GET /carro - listar todos")
    void testPesquisarTodosCarros() throws Exception {
        Carro c1 = new Carro("Civic", 2023);
        c1.setId(1L);
        Carro c2 = new Carro("Gol", 2022);
        c2.setId(2L);

        when(carroService.findAll()).thenReturn(Arrays.asList(c1, c2));

        mockMvc.perform(get("/carro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].modelo").value("Civic"))
                .andExpect(jsonPath("$[1].modelo").value("Gol"));

        verify(carroService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /carro/{id} - encontrado")
    void testPesquisarCarroPorIdEncontrado() throws Exception {
        Carro c = new Carro("Civic", 2023);
        c.setId(1L);

        when(carroService.findById(1L)).thenReturn(Optional.of(c));

        mockMvc.perform(get("/carro/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Civic"));

        verify(carroService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /carro/{id} - não encontrado")
    void testPesquisarCarroPorIdNaoEncontrado() throws Exception {
        when(carroService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/carro/999"))
                .andExpect(status().isNotFound());

        verify(carroService, times(1)).findById(999L);
    }

    @Test
    @DisplayName("PUT /carro/{id} - atualizar")
    @WithMockUser(username = "testuser", roles = {"GERENTE"})
    void testAtualizarCarro() throws Exception {
        Carro c = new Carro("Civic", 2024);
        c.setId(1L);

        when(carroService.update(any(Carro.class))).thenReturn(c);

        mockMvc.perform(put("/carro/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(c)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ano").value(2024));

        verify(carroService, times(1)).update(any(Carro.class));
    }

    @Test
    @DisplayName("DELETE /carro/{id} - deletar")
    @WithMockUser(username = "testuser", roles = {"GERENTE"})
    void testDeletarCarro() throws Exception {
        doNothing().when(carroService).deleteById(1L);

        mockMvc.perform(delete("/carro/1"))
                .andExpect(status().isNoContent());

        verify(carroService, times(1)).deleteById(1L);
    }
}
