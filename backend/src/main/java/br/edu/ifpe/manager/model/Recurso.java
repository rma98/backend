package br.edu.ifpe.manager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_recurso", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public class Recurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "O nome do recurso não pode estar vazio.")
    private String nome;

    @NotEmpty(message = "A descrição do recurso não pode estar vazia.")
    private String descricao;

    @Min(value = 1, message = "A capacidade do recurso deve ser maior que 0.")
    private int capacidade;

    @NotNull(message = "O status do recurso não pode ser nulo.")
    @Enumerated(EnumType.STRING)
    private StatusRecurso status;

    @NotEmpty(message = "A localização do recurso não pode estar vazia.")
    private String localizacao;

    // Relação com reservas
    @OneToMany(mappedBy = "recurso", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Reserva> reservas = new ArrayList<>();

    // Métodos utilitários para sincronizar relações bidirecionais
    public void adicionarReserva(Reserva reserva) {
        reservas.add(reserva);
        reserva.setRecurso(this);
    }

    public void removerReserva(Reserva reserva) {
        reservas.remove(reserva);
        reserva.setRecurso(null);
    }
}
