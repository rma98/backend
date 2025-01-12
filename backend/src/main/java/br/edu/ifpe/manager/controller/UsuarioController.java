package br.edu.ifpe.manager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.edu.ifpe.manager.dto.UsuarioDTO;
import br.edu.ifpe.manager.exception.ErrorResponse;
import br.edu.ifpe.manager.model.LoginRequest;
import br.edu.ifpe.manager.model.Reserva;
import br.edu.ifpe.manager.model.Recurso;
import br.edu.ifpe.manager.model.StatusReserva;
import br.edu.ifpe.manager.model.TipoUsuario;
import br.edu.ifpe.manager.model.Usuario;
import br.edu.ifpe.manager.service.RecursoService;
import br.edu.ifpe.manager.service.UsuarioService;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RecursoService recursoService;

    public UsuarioController(UsuarioService usuarioService, RecursoService recursoService) {
        this.usuarioService = usuarioService;
        this.recursoService = recursoService;  // Inicializa o RecursoService
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        List<Usuario> usuarios = usuarioService.listarTodos();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        try {
            Usuario usuario = usuarioService.buscarUsuarioPorId(id);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Usuario> buscarPorEmail(@PathVariable String email) {
        Optional<Usuario> usuario = usuarioService.buscarPorEmail(email);
        return usuario.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioDTO> obterPerfil(@RequestParam String email) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            UsuarioDTO usuarioDTO = usuarioService.toDTO(usuario);
            return ResponseEntity.ok(usuarioDTO);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping
    public ResponseEntity<?> salvar(@RequestBody @Valid Usuario usuario) {
        if (usuarioService.existeEmail(usuario.getEmail())) {
            // Retorno consistente de erro com ErrorResponse
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "O email já está em uso.", List.of("Email: " + usuario.getEmail())));
        }

        Usuario novoUsuario = usuarioService.salvar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Usuario usuario = usuarioService.login(loginRequest.getEmail(), loginRequest.getSenha());
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Credenciais inválidas", List.of("Email ou senha incorretos.")));
            }

            UsuarioDTO usuarioDTO = usuarioService.toDTO(usuario);
            return ResponseEntity.ok(Map.of(
                    "message", "Login realizado com sucesso",
                    "usuario", usuarioDTO
            ));
        } catch (Exception e) {
            // Aqui, mantemos a consistência na resposta de erro
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Erro no login", List.of("Detalhes do erro: " + e.getMessage())));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPorId(@PathVariable Long id) {
        usuarioService.deletarPorId(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

 // Novo endpoint para realizar a reserva
    @PostMapping("/reservar")
    public ResponseEntity<?> realizarReserva(@RequestParam Long usuarioId,
                                              @RequestParam Long recursoId,
                                              @RequestParam String dataInicio,
                                              @RequestParam String dataFinal) {
        try {
            Usuario usuario = usuarioService.buscarUsuarioPorId(usuarioId);
            Recurso recurso = recursoService.buscarRecursoPorId(recursoId);
            
            LocalDateTime dataInicioLocal = LocalDateTime.parse(dataInicio);
            LocalDateTime dataFinalLocal = LocalDateTime.parse(dataFinal);

            Reserva reserva = usuarioService.realizarReserva(usuario, recurso, dataInicioLocal, dataFinalLocal);
            return ResponseEntity.status(HttpStatus.CREATED).body(reserva);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Erro ao realizar a reserva", List.of("Detalhes do erro: " + e.getMessage())));
        }
    }
}
