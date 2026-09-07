package pe.edu.upeu.bomerp.participacion.registro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResponse;
import pe.edu.upeu.bomerp.participacion.registro.service.RegistroService;
import java.util.List;

@Tag(name = "Participacion")
@RestController
@RequestMapping("/api/v1/participacion/registros")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroService registroService;

    @Operation(summary = "Lista los registros de participacion")
    @GetMapping
    public ResponseEntity<List<RegistroResponse>> listar() {
        return ResponseEntity.ok(registroService.listar());
    }

    @Operation(summary = "Consulta un registro con sus evidencias")
    @GetMapping("/{id}")
    public ResponseEntity<RegistroResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(registroService.obtener(id));
    }

    @Operation(summary = "Registra la participacion diaria consumiendo el cupo de la campania")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse crear(@Valid @RequestBody RegistroRequest request) {
        return registroService.crear(request);
    }
}