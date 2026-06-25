
package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/carro")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CarroController {

    @Autowired
    private CarroService carroService;

    // Salvar carro (apenas GERENTE e VENDEDOR)
    @PostMapping("salvar")
    @PreAuthorize("hasAnyRole('GERENTE', 'VENDEDOR')")
    public ResponseEntity<Carro> salvarCarro(@Valid @RequestBody Carro c) {
        if (c.getModelo() == null || c.getModelo().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (c.getAno() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
        Carro savedCarro = carroService.save(c);
        return ResponseEntity.ok(savedCarro);
    }

    // Atualizar carro (apenas GERENTE)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Carro> atualizarCarro(@PathVariable Long id, @Valid @RequestBody Carro c) {
        if (c.getModelo() == null || c.getModelo().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (c.getAno() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
        c.setId(id);  // Define o ID no objeto
        Carro updatedCarro = carroService.update(c);
        return ResponseEntity.ok(updatedCarro);
    }

    // Deletar carro (apenas GERENTE)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> deletarCarro(@PathVariable Long id) {
        carroService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Pesquisar carro por ID (público)
    @GetMapping("/{id}")
    public ResponseEntity<Carro> pesquisarCarroPorId(@PathVariable Long id) {
        Optional<Carro> carro = carroService.findById(id);
        return carro.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Pesquisar todos os carros (público)
    @GetMapping
    public ResponseEntity<List<Carro>> pesquisarTodosCarros() {
        List<Carro> carros = carroService.findAll();
        return ResponseEntity.ok(carros);
    }
}