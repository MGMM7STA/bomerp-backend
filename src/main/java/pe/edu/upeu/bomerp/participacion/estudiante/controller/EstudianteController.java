package pe.edu.upeu.bomerp.participacion.estudiante.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.bomerp.participacion.estudiante.dto.EstudianteResponse;
import pe.edu.upeu.bomerp.participacion.estudiante.service.EstudianteService;
import java.util.List;

@Tag(name = "Estudiantes")
@RestController
@RequestMapping("/api/v1/participacion/estudiantes")
@RequiredArgsConstructor
public class EstudianteController {

    private final EstudianteService estudianteService;

    @Operation(summary = "Devuelve el estudiante del usuario que inicio sesion, enlazandolo la primera vez")
    @GetMapping("/yo")
    public ResponseEntity<EstudianteResponse> yo(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(estudianteService.resolver(jwt));
    }

    @Operation(summary = "Lista los estudiantes registrados")
    @GetMapping
    public ResponseEntity<List<EstudianteResponse>> listar() {
        return ResponseEntity.ok(estudianteService.listar());
    }
}
