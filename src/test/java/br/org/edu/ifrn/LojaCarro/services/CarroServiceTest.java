package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.CarroException;
import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes da Classe CarroService")
public class CarroServiceTest {

    @Mock
    private CarroRepository carroRepository;

    @InjectMocks
    private CarroService carroService;

    private Carro carroValido;
    private Carro carroComModeloGrande;

    @BeforeEach
    void setUp() {
        carroValido = new Carro("Civic", 2023);
        carroComModeloGrande = new Carro("FordMustangGT500", 2023);
    }

    // ==================== Testes para save() ====================

    @Test
    @DisplayName("save: Deve salvar carro com modelo válido (menos de 10 caracteres)")
    void testSaveCarroValido() {
        // Arrange
        when(carroRepository.save(carroValido)).thenReturn(carroValido);

        // Act
        Carro resultado = carroService.save(carroValido);

        // Assert
        assertNotNull(resultado);
        assertEquals("Civic", resultado.getModelo());
        verify(carroRepository, times(1)).save(carroValido);
    }

    @Test
    @DisplayName("save: Deve lançar exceção quando modelo é vazio")
    void testSaveCarroComModeloVazio() {
        // Arrange
        Carro carroVazio = new Carro("", 2023);

        // Act & Assert
        CarroException exception = assertThrows(CarroException.class, () -> {
            carroService.save(carroVazio);
        });

        assertEquals("O modelo do carro não pode estar vazio.", exception.getMessage());
        verify(carroRepository, never()).save(any());
    }

    @Test
    @DisplayName("save: Deve lançar exceção quando modelo é null")
    void testSaveCarroComModeloNull() {
        // Arrange
        Carro carroNull = new Carro(null, 2023);

        // Act & Assert
        CarroException exception = assertThrows(CarroException.class, () -> {
            carroService.save(carroNull);
        });

        assertEquals("O modelo do carro não pode estar vazio.", exception.getMessage());
        verify(carroRepository, never()).save(any());
    }

    @Test
    @DisplayName("save: Deve lançar exceção quando modelo possui 10 ou mais caracteres")
    void testSaveCarroComModeloMuitoGrande() {
        // Arrange (modelo com 15 caracteres)
        Carro carroGrande = new Carro("FordMustangGT500", 2023);

        // Act & Assert
        CarroException exception = assertThrows(CarroException.class, () -> {
            carroService.save(carroGrande);
        });

        assertTrue(exception.getMessage().contains("O modelo do carro deve ter menos de 10 caracteres"));
        verify(carroRepository, never()).save(any());
    }

    @Test
    @DisplayName("save: Deve lançar exceção quando modelo possui exatamente 10 caracteres")
    void testSaveCarroComModeloExatamente10Caracteres() {
        // Arrange (modelo com 10 caracteres: "Civic2023x")
        Carro carroComDez = new Carro("Civic2023x", 2023);

        // Act & Assert
        CarroException exception = assertThrows(CarroException.class, () -> {
            carroService.save(carroComDez);
        });

        assertTrue(exception.getMessage().contains("O modelo do carro deve ter menos de 10 caracteres"));
        verify(carroRepository, never()).save(any());
    }

    @Test
    @DisplayName("save: Deve salvar carro com modelo limite (9 caracteres)")
    void testSaveCarroComModeloLimite() {
        // Arrange (modelo com 9 caracteres: "Civic2023")
        Carro carroLimite = new Carro("Civic2023", 2023);
        when(carroRepository.save(carroLimite)).thenReturn(carroLimite);

        // Act
        Carro resultado = carroService.save(carroLimite);

        // Assert
        assertNotNull(resultado);
        assertEquals("Civic2023", resultado.getModelo());
        verify(carroRepository, times(1)).save(carroLimite);
    }

    // ==================== Testes para deleteById() ====================

    @Test
    @DisplayName("deleteById: Deve deletar carro com ID válido")
    void testDeleteByIdValido() {
        // Arrange
        Long idValido = 1L;

        // Act
        carroService.deleteById(idValido);

        // Assert
        verify(carroRepository, times(1)).deleteById(idValido);
    }

    @Test
    @DisplayName("save: Deve lançar exceção quando preço for negativo")
    void testSaveCarroComPrecoNegativo() {
        // Arrange
        Carro carroPrecoNegativo = new Carro("Civic", 2023);
        carroPrecoNegativo.setPreco(-5000.0);

        // Act & Assert
        CarroException exception = assertThrows(CarroException.class, () -> {
            carroService.save(carroPrecoNegativo);
        });

        assertTrue(exception.getMessage().contains("O preço do carro não pode ser negativo"));
        verify(carroRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteById: Deve lançar exceção quando ID é negativo")
    void testDeleteByIdNegativo() {
        // Arrange
        Long idNegativo = -1L;

        // Act & Assert
        CarroException exception = assertThrows(CarroException.class, () -> {
            carroService.deleteById(idNegativo);
        });

        assertEquals("O ID do carro não pode ser negativo. ID fornecido: -1", exception.getMessage());
        verify(carroRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteById: Deve lançar exceção quando ID é zero")
    void testDeleteByIdZero() {
        // Arrange
        Long idZero = 0L;

        // Act & Assert
        CarroException exception = assertThrows(CarroException.class, () -> {
            carroService.deleteById(idZero);
        });

        assertEquals("O ID do carro não pode ser negativo. ID fornecido: 0", exception.getMessage());
        verify(carroRepository, never()).deleteById(any());
    }

    // ==================== Testes para findById() ====================

    @Test
    @DisplayName("findById: Deve retornar carro quando ID é válido e existe")
    void testFindByIdValido() {
        // Arrange
        Long idValido = 1L;
        when(carroRepository.findById(idValido)).thenReturn(Optional.of(carroValido));

        // Act
        Optional<Carro> resultado = carroService.findById(idValido);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Civic", resultado.get().getModelo());
        verify(carroRepository, times(1)).findById(idValido);
    }

    @Test
    @DisplayName("findById: Deve retornar Optional vazio quando carro não existe")
    void testFindByIdNaoExiste() {
        // Arrange
        Long idInexistente = 999L;
        when(carroRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Act
        Optional<Carro> resultado = carroService.findById(idInexistente);

        // Assert
        assertTrue(resultado.isEmpty());
        verify(carroRepository, times(1)).findById(idInexistente);
    }

    @Test
    @DisplayName("findById: Deve lançar exceção quando ID é negativo")
    void testFindByIdNegativo() {
        // Arrange
        Long idNegativo = -1L;

        // Act & Assert
        CarroException exception = assertThrows(CarroException.class, () -> {
            carroService.findById(idNegativo);
        });

        assertEquals("O ID do carro não pode ser negativo. ID fornecido: -1", exception.getMessage());
        verify(carroRepository, never()).findById(any());
    }

    @Test
    @DisplayName("findById: Deve lançar exceção quando ID é zero")
    void testFindByIdZero() {
        // Arrange
        Long idZero = 0L;

        // Act & Assert
        CarroException exception = assertThrows(CarroException.class, () -> {
            carroService.findById(idZero);
        });

        assertEquals("O ID do carro não pode ser negativo. ID fornecido: 0", exception.getMessage());
        verify(carroRepository, never()).findById(any());
    }

    // ==================== Testes para findAll() ====================

    @Test
    @DisplayName("findAll: Deve retornar lista de carros")
    void testFindAllComCarros() {
        // Arrange
        Carro carro2 = new Carro("Gol", 2022);
        List<Carro> carros = Arrays.asList(carroValido, carro2);
        when(carroRepository.findAll()).thenReturn(carros);

        // Act
        List<Carro> resultado = carroService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Civic", resultado.get(0).getModelo());
        assertEquals("Gol", resultado.get(1).getModelo());
        verify(carroRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAll: Deve retornar lista vazia quando não há carros")
    void testFindAllSemCarros() {
        // Arrange
        when(carroRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Carro> resultado = carroService.findAll();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(carroRepository, times(1)).findAll();
    }

    // ==================== Testes para update() ====================

    @Test
    @DisplayName("update: Deve atualizar carro com modelo válido (menos de 10 caracteres)")
    void testUpdateCarroValido() {
        // Arrange
        carroValido.setId(1L);
        when(carroRepository.existsById(1L)).thenReturn(true);
        when(carroRepository.save(carroValido)).thenReturn(carroValido);

        // Act
        Carro resultado = carroService.update(carroValido);

        // Assert
        assertNotNull(resultado);
        assertEquals("Civic", resultado.getModelo());
        verify(carroRepository, times(1)).save(carroValido);
    }

    @Test
    @DisplayName("update: Deve lançar exceção quando modelo é vazio")
    void testUpdateCarroComModeloVazio() {
        // Arrange
        Carro carroVazio = new Carro("", 2023);

        carroVazio.setId(2L);
        when(carroRepository.existsById(2L)).thenReturn(true);

        // Act & Assert
        CarroException exception = assertThrows(CarroException.class, () -> {
            carroService.update(carroVazio);
        });

        assertEquals("O modelo do carro não pode estar vazio.", exception.getMessage());
        verify(carroRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: Deve lançar exceção quando modelo é null")
    void testUpdateCarroComModeloNull() {
        // Arrange
        Carro carroNull = new Carro(null, 2023);

        carroNull.setId(3L);
        when(carroRepository.existsById(3L)).thenReturn(true);

        // Act & Assert
        CarroException exception = assertThrows(CarroException.class, () -> {
            carroService.update(carroNull);
        });

        assertEquals("O modelo do carro não pode estar vazio.", exception.getMessage());
        verify(carroRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: Deve lançar exceção quando modelo possui 10 ou mais caracteres")
    void testUpdateCarroComModeloMuitoGrande() {
        // Arrange (modelo com 15 caracteres)
        Carro carroGrande = new Carro("FordMustangGT500", 2023);

        carroGrande.setId(4L);
        when(carroRepository.existsById(4L)).thenReturn(true);

        // Act & Assert
        CarroException exception = assertThrows(CarroException.class, () -> {
            carroService.update(carroGrande);
        });

        assertTrue(exception.getMessage().contains("O modelo do carro deve ter menos de 10 caracteres"));
        verify(carroRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: Deve atualizar carro com modelo limite (9 caracteres)")
    void testUpdateCarroComModeloLimite() {
        // Arrange (modelo com 9 caracteres: "Civic2023")
        Carro carroLimite = new Carro("Civic2023", 2023);
        carroLimite.setId(5L);
        when(carroRepository.existsById(5L)).thenReturn(true);
        when(carroRepository.save(carroLimite)).thenReturn(carroLimite);

        // Act
        Carro resultado = carroService.update(carroLimite);

        // Assert
        assertNotNull(resultado);
        assertEquals("Civic2023", resultado.getModelo());
        verify(carroRepository, times(1)).save(carroLimite);
    }

    @Test
    @DisplayName("update: Deve lançar exceção quando modelo possui espaços em branco apenas")
    void testUpdateCarroComModeloSoEspacos() {
        // Arrange
        Carro carroEspacos = new Carro("   ", 2023);
        carroEspacos.setId(6L);
        when(carroRepository.existsById(6L)).thenReturn(true);

        // Act & Assert
        CarroException exception = assertThrows(CarroException.class, () -> {
            carroService.update(carroEspacos);
        });

        assertEquals("O modelo do carro não pode estar vazio.", exception.getMessage());
        verify(carroRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: Deve lançar exceção quando carro inexistente")
    void testUpdateCarroInexistente() {
        // Arrange
        Carro c = new Carro("Civic", 2023);
        c.setId(999L);
        when(carroRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        CarroException exception = assertThrows(CarroException.class, () -> {
            carroService.update(c);
        });

        assertTrue(exception.getMessage().contains("não encontrado para atualização"));
        verify(carroRepository, never()).save(any());
    }
}

