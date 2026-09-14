package pe.edu.upeu.bomerp.participacion.registro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
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

    @Operation(summary = "Consulta registros con filtros combinados y ordenamiento opcionales")
    @GetMapping
    public ResponseEntity<List<RegistroResponse>> buscar(
            @RequestParam(required = false) EstadoRegistro estado,
            @RequestParam(required = false) Long estudianteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(defaultValue = "fecha") String ordenarPor,
            @RequestParam(defaultValue = "DESC") String direccion) {
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
