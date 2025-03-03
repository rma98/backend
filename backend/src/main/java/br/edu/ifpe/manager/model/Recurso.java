package br.edu.ifpe.manager.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_recurso", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @NotEmpty(message = "A localização do recurso não pode estar vazia.")
    private String localizacao;
    
    @ManyToMany(mappedBy = "recursosReservados")
    private List<Usuario> usuarios = new ArrayList<>();
    
    @OneToMany(mappedBy = "recurso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reserva> reservas = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    private StatusReserva status;
    
    public Recurso(Long id, String nome, StatusReserva status) {
        this.id = id;
        this.nome = nome;
        this.status = status;
    }
}
