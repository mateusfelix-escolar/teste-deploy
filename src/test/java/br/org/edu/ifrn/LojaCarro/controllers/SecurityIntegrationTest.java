package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.dto.LoginRequest;
import br.org.edu.ifrn.LojaCarro.model.Role;
import br.org.edu.ifrn.LojaCarro.model.User;
import br.org.edu.ifrn.LojaCarro.repository.RoleRepository;
import br.org.edu.ifrn.LojaCarro.repository.UserRepository;
import br.org.edu.ifrn.LojaCarro.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String gerenteToken;
    private String vendedorToken;
    private String clienteToken;

    @BeforeEach
    public void setUp() throws Exception {
        userRepository.deleteAll();

        // Criar usuário GERENTE
        User gerente = new User("gerente_test", "gerente@test.com", 
                passwordEncoder.encode("gerente123"), "Gerente Test");
        Set<Role> gerenteRoles = new HashSet<>();
        Role gerenteRole = roleRepository.findByName(Role.RoleType.ROLE_GERENTE).orElseThrow();
        gerenteRoles.add(gerenteRole);
        gerente.setRoles(gerenteRoles);
        userRepository.save(gerente);

        // Criar usuário VENDEDOR
        User vendedor = new User("vendedor_test", "vendedor@test.com", 
                passwordEncoder.encode("vendedor123"), "Vendedor Test");
        Set<Role> vendedorRoles = new HashSet<>();
        Role vendedorRole = roleRepository.findByName(Role.RoleType.ROLE_VENDEDOR).orElseThrow();
        vendedorRoles.add(vendedorRole);
        vendedor.setRoles(vendedorRoles);
        userRepository.save(vendedor);

        // Criar usuário CLIENTE
        User cliente = new User("cliente_test", "cliente@test.com", 
                passwordEncoder.encode("cliente123"), "Cliente Test");
        Set<Role> clienteRoles = new HashSet<>();
        Role clienteRole = roleRepository.findByName(Role.RoleType.ROLE_CLIENTE).orElseThrow();
        clienteRoles.add(clienteRole);
        cliente.setRoles(clienteRoles);
        userRepository.save(cliente);

        // Gerar tokens
        gerenteToken = "Bearer " + jwtTokenProvider.generateTokenFromUsername("gerente_test");
        vendedorToken = "Bearer " + jwtTokenProvider.generateTokenFromUsername("vendedor_test");
        clienteToken = "Bearer " + jwtTokenProvider.generateTokenFromUsername("cliente_test");
    }

    // ====== TESTES DE VALIDAÇÃO CONTRA XSS ======

    @Test
    public void testXSSInjectionEmModelo() throws Exception {
        String xssPayload = "{\"modelo\": \"<script>alert('XSS')</script>\", \"ano\": 2024, \"preco\": 50000}";
        
        mockMvc.perform(post("/carro/salvar")
                .header("Authorization", gerenteToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(xssPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testValidacaoModeloVazio() throws Exception {
        String payload = "{\"modelo\": \"\", \"ano\": 2024, \"preco\": 50000}";
        
        mockMvc.perform(post("/carro/salvar")
                .header("Authorization", gerenteToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testValidacaoAnoInvalido() throws Exception {
        String payload = "{\"modelo\": \"Corolla\", \"ano\": 1800, \"preco\": 50000}";
        
        mockMvc.perform(post("/carro/salvar")
                .header("Authorization", gerenteToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testValidacaoPrecoNegativo() throws Exception {
        String payload = "{\"modelo\": \"Corolla\", \"ano\": 2024, \"preco\": -100}";
        
        mockMvc.perform(post("/carro/salvar")
                .header("Authorization", gerenteToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

    // ====== TESTES DE ACESSO NÃO AUTORIZADO ======

    @Test
    public void testAcessoSemAutenticacao() throws Exception {
        mockMvc.perform(post("/carro/salvar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"modelo\": \"Corolla\", \"ano\": 2024, \"preco\": 50000}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testTokenInvalido() throws Exception {
        mockMvc.perform(post("/carro/salvar")
                .header("Authorization", "Bearer token_invalido_123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"modelo\": \"Corolla\", \"ano\": 2024, \"preco\": 50000}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testClienteNaoPoderCadastrarCarro() throws Exception {
        String payload = "{\"modelo\": \"Corolla\", \"ano\": 2024, \"preco\": 50000}";
        
        mockMvc.perform(post("/carro/salvar")
                .header("Authorization", clienteToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isForbidden());
    }

    // ====== TESTES DE AUTORIZAÇÃO POR ROLES ======

    @Test
    public void testGerentePoderCadastrarCarro() throws Exception {
        String payload = "{\"modelo\": \"Corolla\", \"ano\": 2024, \"preco\": 50000}";
        
        mockMvc.perform(post("/carro/salvar")
                .header("Authorization", gerenteToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    public void testVendedorPoderCadastrarCarro() throws Exception {
        String payload = "{\"modelo\": \"Civic\", \"ano\": 2024, \"preco\": 60000}";
        
        mockMvc.perform(post("/carro/salvar")
                .header("Authorization", vendedorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    public void testApenasGerentePoderAtualizarCarro() throws Exception {
        // Testar apenas se o vendedor consegue fazer a requisição de update (ele não consegue)
        String payload = "{\"modelo\": \"Corolla\", \"ano\": 2025, \"preco\": 55000}";
        
        // Vendedor não pode atualizar (acesso negado)
        mockMvc.perform(put("/carro/1")
                .header("Authorization", vendedorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().is4xxClientError()); // Pode ser 400 ou 403
    }

    @Test
    public void testApenasGerentePoderDeletarCarro() throws Exception {
        // Vendedor não pode deletar
        mockMvc.perform(delete("/carro/1")
                .header("Authorization", vendedorToken))
                .andExpect(status().isForbidden());

        // Gerente pode deletar
        mockMvc.perform(delete("/carro/1")
                .header("Authorization", gerenteToken))
                .andExpect(status().isNoContent());
    }

    // ====== TESTES DE ACESSO PÚBLICO ======

    @Test
    public void testLerCarroPublico() throws Exception {
        mockMvc.perform(get("/carro")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testLerCarroPorIdPublico() throws Exception {
        mockMvc.perform(get("/carro/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 404 porque não existe, mas não é 401
    }

    // ====== TESTES DE AUTENTICAÇÃO ======

    @Test
    public void testLoginComCredenciaisValidas() throws Exception {
        LoginRequest loginRequest = new LoginRequest("gerente_test", "gerente123");
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.username", is("gerente_test")));
    }

    @Test
    public void testLoginComCredenciaisInvalidas() throws Exception {
        LoginRequest loginRequest = new LoginRequest("gerente_test", "senha_errada");
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError()); // 400 ou 401
    }

    @Test
    public void testValidacaoSenhaVazia() throws Exception {
        LoginRequest loginRequest = new LoginRequest("gerente_test", "");
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    // ====== TESTES DE PROTEÇÃO CONTRA SQL INJECTION ======

    @Test
    public void testSQLInjectionNoModelo() throws Exception {
        String sqlInjectionPayload = "{\"modelo\": \"'; DROP TABLE carros; --\", \"ano\": 2024, \"preco\": 50000}";
        
        mockMvc.perform(post("/carro/salvar")
                .header("Authorization", gerenteToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(sqlInjectionPayload))
                .andExpect(status().isBadRequest()); // Deve rejeitar caracteres inválidos
    }

    @Test
    public void testStringMuitoGrandeNoModelo() throws Exception {
        String modeloGrande = "a".repeat(1000); // 1000 caracteres
        String payload = "{\"modelo\": \"" + modeloGrande + "\", \"ano\": 2024, \"preco\": 50000}";
        
        mockMvc.perform(post("/carro/salvar")
                .header("Authorization", gerenteToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }
}
