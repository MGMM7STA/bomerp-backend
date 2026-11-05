package pe.edu.upeu.bomerp.participacion.registro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import pe.edu.upeu.bomerp.participacion.estudiante.service.EstudianteService;
import pe.edu.upeu.bomerp.participacion.registro.dto.RechazoRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroReporte;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResponse;
import pe.edu.upeu.bomerp.participacion.registro.entity.EstadoRegistro;
import pe.edu.upeu.bomerp.participacion.registro.service.RegistroService;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Tag(name = "Participacion")
@RestController
@RequestMapping("/api/v1/participacion/registros")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroService registroService;
    private final EstudianteService estudianteService;

    @Operation(summary = "Consulta registros con filtros combinados y ordenamiento opcionales")
    @GetMapping
    public ResponseEntity<List<RegistroResponse>> buscar(
            @RequestParam(required = false) EstadoRegistro estado,
            @RequestParam(required = false) Long estudianteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(defaultValue = "fecha") String ordenarPor,
            @RequestParam(defaultValue = "DESC") String direccion,
            @AuthenticationPrincipal Jwt jwt,
            Authentication autenticacion) {
        if (!esCoordinacion(autenticacion)) {
            estudianteId = estudianteService.resolver(jwt).getId();
        }
        log.info("Consultando registros estado={} estudianteId={} desde={} hasta={} ordenarPor={} direccion={}",
                estado, estudianteId, desde, hasta, ordenarPor, direccion);
        return ResponseEntity.ok(
                registroService.buscar(estado, estudianteId, desde, hasta, ordenarPor, direccion));
    }

    @Operation(summary = "Genera un reporte de participacion: agregados y detalle resumido")
    @GetMapping("/resumen")
    public ResponseEntity<RegistroReporte> resumen(
            @RequestParam(required = false) EstadoRegistro estado,
            @RequestParam(required = false) Long estudianteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(registroService.reporte(estado, estudianteId, desde, hasta));
    }

    @Operation(summary = "Consulta un registro con sus evidencias")
    @GetMapping("/{id}")
    public ResponseEntity<RegistroResponse> obtener(@PathVariable Long id,
                                                    @AuthenticationPrincipal Jwt jwt,
                                                    Authentication autenticacion) {
        RegistroResponse registro = registroService.obtener(id);
        if (!esCoordinacion(autenticacion)
                && !registro.getEstudianteId().equals(estudianteService.resolver(jwt).getId())) {
            throw new ResourceNotFoundException("Registro no encontrado: " + id);
        }
        return ResponseEntity.ok(registro);
    }

    @Operation(summary = "Registra la participacion diaria consumiendo el cupo de la campania")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse crear(@Valid @RequestBody RegistroRequest request,
                                  @AuthenticationPrincipal Jwt jwt) {
        Long estudianteId = estudianteService.resolver(jwt).getId();
        return registroService.crear(request, estudianteId);
    }

    @Operation(summary = "Anula un registro y devuelve el cupo diario consumido por sus evidencias")
    @PatchMapping("/{id}/anular")
    public ResponseEntity<RegistroResponse> anular(@PathVariable Long id) {
        log.info("Anulando registro id={}", id);
        return ResponseEntity.ok(registroService.anular(id));
    }

    @Operation(summary = "Valida un registro enviado: sus evidencias pasan a sumar puntaje")
    @PatchMapping("/{id}/validar")
    public ResponseEntity<RegistroResponse> validar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        log.info("Validando registro id={}", id);
        return ResponseEntity.ok(registroService.validar(id, nombreDe(jwt)));
    }

    @Operation(summary = "Rechaza un registro enviado, con motivo, y devuelve su cupo diario")
    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<RegistroResponse> rechazar(@PathVariable Long id,
                                                     @Valid @RequestBody RechazoRequest request,
                                                     @AuthenticationPrincipal Jwt jwt) {
        log.info("Rechazando registro id={}", id);
        return ResponseEntity.ok(registroService.rechazar(id, request.getMotivo(), nombreDe(jwt)));
    }

    private boolean esCoordinacion(Authentication autenticacion) {
        return autenticacion.getAuthorities().stream()
                .anyMatch(rol -> rol.getAuthority().equals("ROLE_COORDINADOR")
                        || rol.getAuthority().equals("ROLE_ADMIN"));
    }

    private String nombreDe(Jwt jwt) {
        String nombre = jwt.getClaimAsString("name");
        return nombre != null ? nombre : jwt.getClaimAsString("preferred_username");
    }
}
