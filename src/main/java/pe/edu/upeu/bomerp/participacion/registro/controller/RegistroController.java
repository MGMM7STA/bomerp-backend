package pe.edu.upeu.bomerp.participacion.registro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import pe.edu.upeu.bomerp.participacion.estudiante.service.EstudianteService;
import pe.edu.upeu.bomerp.participacion.registro.dto.FotoEvidencia;
import pe.edu.upeu.bomerp.participacion.registro.dto.RechazoRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroReporte;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResponse;
import pe.edu.upeu.bomerp.participacion.registro.entity.EstadoRegistro;
import pe.edu.upeu.bomerp.participacion.registro.service.RegistroService;
import pe.edu.upeu.bomerp.sedes.Sede;
import pe.edu.upeu.bomerp.sedes.SedeService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Tag(name = "Participacion")
@RestController
@RequestMapping("/api/v1/participacion/registros")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroService registroService;
    private final EstudianteService estudianteService;
    private final SedeService sedeService;

    @Operation(summary = "Consulta registros con filtros combinados y ordenamiento opcionales")
    @GetMapping
    public ResponseEntity<List<RegistroResponse>> buscar(
            @RequestParam(required = false) EstadoRegistro estado,
            @RequestParam(required = false) Long estudianteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(defaultValue = "fecha") String ordenarPor,
            @RequestParam(defaultValue = "DESC") String direccion,
            @RequestParam(required = false) Sede sede,
            @AuthenticationPrincipal Jwt jwt,
            Authentication autenticacion) {
        if (!esCoordinacion(autenticacion)) {
            estudianteId = estudianteService.resolver(jwt).getId();
        }
        Sede alcance = sedeService.alcance(jwt, sede).orElse(null);
        log.info("Consultando registros estado={} estudianteId={} desde={} hasta={} ordenarPor={} direccion={}",
                estado, estudianteId, desde, hasta, ordenarPor, direccion);
        return ResponseEntity.ok(
                registroService.buscar(estado, estudianteId, desde, hasta, ordenarPor, direccion, alcance));
    }

    @Operation(summary = "Genera un reporte de participacion: agregados y detalle resumido")
    @GetMapping("/resumen")
    public ResponseEntity<RegistroReporte> resumen(
            @RequestParam(required = false) EstadoRegistro estado,
            @RequestParam(required = false) Long estudianteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) Sede sede,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(registroService.reporte(estado, estudianteId, desde, hasta,
                sedeService.alcance(jwt, sede).orElse(null)));
    }

    @Operation(summary = "Consulta un registro con sus evidencias")
    @GetMapping("/{id}")
    public ResponseEntity<RegistroResponse> obtener(@PathVariable Long id,
                                                    @AuthenticationPrincipal Jwt jwt,
                                                    Authentication autenticacion) {
        return ResponseEntity.ok(visible(id, jwt, autenticacion));
    }

    @Operation(summary = "Registra la participacion diaria consumiendo el cupo de la campania")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse crear(@Valid @RequestBody RegistroRequest request,
                                  @AuthenticationPrincipal Jwt jwt) {
        Long estudianteId = estudianteService.resolver(jwt).getId();
        return registroService.crear(request, estudianteId, sedeService.requerida(jwt, null));
    }

    @Operation(summary = "Registra la participacion con una foto por evidencia (partes foto-0, foto-1...)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse crearConFotos(@Valid @RequestPart("registro") RegistroRequest request,
                                          MultipartHttpServletRequest peticion,
                                          @AuthenticationPrincipal Jwt jwt) {
        Long estudianteId = estudianteService.resolver(jwt).getId();
        Map<Integer, MultipartFile> fotos = new HashMap<>();
        peticion.getFileMap().forEach((nombre, archivo) -> {
            if (nombre.startsWith("foto-")) {
                fotos.put(Integer.parseInt(nombre.substring(5)), archivo);
            }
        });
        log.info("Registrando participacion estudianteId={} con {} fotos", estudianteId, fotos.size());
        return registroService.crear(request, estudianteId, sedeService.requerida(jwt, null), fotos);
    }

    @Operation(summary = "Devuelve la foto de una evidencia")
    @GetMapping("/{id}/detalles/{detalleId}/foto")
    public ResponseEntity<Resource> foto(@PathVariable Long id,
                                         @PathVariable Long detalleId,
                                         @AuthenticationPrincipal Jwt jwt,
                                         Authentication autenticacion) {
        visible(id, jwt, autenticacion);
        FotoEvidencia foto = registroService.foto(id, detalleId);
        return ResponseEntity.ok()
                .contentType(foto.tipo())
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
                .body(foto.contenido());
    }

    @Operation(summary = "Valida una evidencia del registro")
    @PatchMapping("/{id}/detalles/{detalleId}/validar")
    public ResponseEntity<RegistroResponse> validarDetalle(@PathVariable Long id,
                                                           @PathVariable Long detalleId,
                                                           @AuthenticationPrincipal Jwt jwt,
                                                           Authentication autenticacion) {
        visible(id, jwt, autenticacion);
        log.info("Validando evidencia id={} del registro id={}", detalleId, id);
        return ResponseEntity.ok(registroService.validarDetalle(id, detalleId, nombreDe(jwt)));
    }

    @Operation(summary = "Rechaza una evidencia del registro, con motivo, y devuelve su cupo")
    @PatchMapping("/{id}/detalles/{detalleId}/rechazar")
    public ResponseEntity<RegistroResponse> rechazarDetalle(@PathVariable Long id,
                                                            @PathVariable Long detalleId,
                                                            @Valid @RequestBody RechazoRequest request,
                                                            @AuthenticationPrincipal Jwt jwt,
                                                            Authentication autenticacion) {
        visible(id, jwt, autenticacion);
        log.info("Rechazando evidencia id={} del registro id={}", detalleId, id);
        return ResponseEntity.ok(
                registroService.rechazarDetalle(id, detalleId, request.getMotivo(), nombreDe(jwt)));
    }

    @Operation(summary = "Anula un registro y devuelve el cupo diario consumido por sus evidencias")
    @PatchMapping("/{id}/anular")
    public ResponseEntity<RegistroResponse> anular(@PathVariable Long id,
                                                   @AuthenticationPrincipal Jwt jwt,
                                                   Authentication autenticacion) {
        visible(id, jwt, autenticacion);
        log.info("Anulando registro id={}", id);
        return ResponseEntity.ok(registroService.anular(id));
    }

    @Operation(summary = "Valida un registro enviado: sus evidencias pasan a sumar puntaje")
    @PatchMapping("/{id}/validar")
    public ResponseEntity<RegistroResponse> validar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt,
                                                    Authentication autenticacion) {
        visible(id, jwt, autenticacion);
        log.info("Validando registro id={}", id);
        return ResponseEntity.ok(registroService.validar(id, nombreDe(jwt)));
    }

    @Operation(summary = "Rechaza un registro enviado, con motivo, y devuelve su cupo diario")
    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<RegistroResponse> rechazar(@PathVariable Long id,
                                                     @Valid @RequestBody RechazoRequest request,
                                                     @AuthenticationPrincipal Jwt jwt,
                                                     Authentication autenticacion) {
        visible(id, jwt, autenticacion);
        log.info("Rechazando registro id={}", id);
        return ResponseEntity.ok(registroService.rechazar(id, request.getMotivo(), nombreDe(jwt)));
    }

    private RegistroResponse visible(Long id, Jwt jwt, Authentication autenticacion) {
        RegistroResponse registro = registroService.obtener(id);
        if (esCoordinacion(autenticacion)) {
            sedeService.alcance(jwt, null)
                    .filter(sede -> !sede.name().equals(registro.getSede()))
                    .ifPresent(sede -> {
                        throw new ResourceNotFoundException("Registro no encontrado: " + id);
                    });
        } else if (!registro.getEstudianteId().equals(estudianteService.resolver(jwt).getId())) {
            throw new ResourceNotFoundException("Registro no encontrado: " + id);
        }
        return registro;
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
