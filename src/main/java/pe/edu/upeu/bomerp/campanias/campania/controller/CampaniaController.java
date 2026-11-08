package pe.edu.upeu.bomerp.campanias.campania.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestParam;
import pe.edu.upeu.bomerp.sedes.Sede;
import pe.edu.upeu.bomerp.sedes.SedeService;
import pe.edu.upeu.bomerp.auditoria.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaRequest;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaResponse;
import pe.edu.upeu.bomerp.campanias.campania.service.CampaniaService;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import java.util.List;

@Slf4j
@Tag(name = "Campanias")
@RestController
@RequestMapping("/api/v1/campanias")
@RequiredArgsConstructor
public class CampaniaController {

    private final CampaniaService campaniaService;
    private final SedeService sedeService;
    private final AuditoriaService auditoria;

    @Operation(summary = "Lista las campanias")
    @GetMapping
    public ResponseEntity<List<CampaniaResponse>> listar(@RequestParam(required = false) Sede sede,
                                                        @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(campaniaService.listar(sedeService.alcance(jwt, sede).orElse(null)));
    }

    @Operation(summary = "Devuelve la campania activa con su cupo diario")
    @GetMapping("/activa")
    public ResponseEntity<CampaniaResponse> obtenerActiva(@RequestParam(required = false) Sede sede,
                                                          @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(campaniaService.obtenerActiva(sedeService.requerida(jwt, sede)));
    }

    @Operation(summary = "Lista los habitos que se practican en la campania activa")
    @GetMapping("/activa/habitos")
    public ResponseEntity<List<HabitoResponse>> habitosDeActiva(@RequestParam(required = false) Sede sede,
                                                                @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(campaniaService.habitosDeActiva(sedeService.requerida(jwt, sede)));
    }

    @Operation(summary = "Consulta una campania por id")
    @GetMapping("/{id}")
    public ResponseEntity<CampaniaResponse> obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        CampaniaResponse campania = campaniaService.obtener(id);
        sedeService.alcance(jwt, null)
                .filter(sede -> !campania.getSedes().contains(sede.name()))
                .ifPresent(sede -> {
                    throw new ResourceNotFoundException("Campania no encontrada con id: " + id);
                });
        return ResponseEntity.ok(campania);
    }

    @Operation(summary = "Registra una campania")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampaniaResponse crear(@Valid @RequestBody CampaniaRequest request, @AuthenticationPrincipal Jwt jwt) {
        CampaniaResponse campania = campaniaService.crear(request);
        auditoria.registrar(jwt, "CREAR_CAMPANIA", "CAMPANIA", campania.getId(), resumen(campania), null);
        return campania;
    }

    @Operation(summary = "Actualiza una campania")
    @PutMapping("/{id}")
    public ResponseEntity<CampaniaResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody CampaniaRequest request,
                                                      @AuthenticationPrincipal Jwt jwt) {
        CampaniaResponse campania = campaniaService.actualizar(id, request);
        auditoria.registrar(jwt, "EDITAR_CAMPANIA", "CAMPANIA", campania.getId(), resumen(campania), null);
        return ResponseEntity.ok(campania);
    }

    @Operation(summary = "Elimina una campania sin registros de participacion")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        CampaniaResponse campania = campaniaService.obtener(id);
        campaniaService.eliminar(id);
        auditoria.registrar(jwt, "ELIMINAR_CAMPANIA", "CAMPANIA", id, campania.getNombre(), null);
    }

    private String resumen(CampaniaResponse campania) {
        return campania.getNombre() + " - " + campania.getEstado() + " - sedes " + String.join(", ", campania.getSedes());
    }
}
