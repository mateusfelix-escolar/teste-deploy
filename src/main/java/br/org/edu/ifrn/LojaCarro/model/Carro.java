package br.org.edu.ifrn.LojaCarro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class Carro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(min = 1, max = 255, message = "Modelo deve ter entre 1 e 255 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-áéíóúâêôãõçÁÉÍÓÚÂÊÔÃÕÇ()]*$", message = "Modelo contém caracteres inválidos")
    String modelo;

    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1900, message = "Ano deve ser no mínimo 1900")
    @Max(value = 2100, message = "Ano deve ser no máximo 2100")
    int ano;

    @NotNull(message = "Preço é obrigatório")
    @PositiveOrZero(message = "Preço deve ser positivo")
    double preco;

    // No-arg constructor necessário para desserialização (Jackson)
    public Carro() {
    }

    public Carro(String modelo, int ano) {
        this.modelo = modelo;
        this.ano = ano;
        this.preco = 0.0;
    }

    public Carro(String modelo, int ano, double preco) {
        this.modelo = modelo;
        this.ano = ano;
        this.preco = preco;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }
}
