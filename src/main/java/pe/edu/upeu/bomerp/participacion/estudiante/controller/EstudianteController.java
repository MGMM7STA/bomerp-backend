package pe.edu.upeu.bomerp.participacion.estudiante.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.bomerp.participacion.estudiante.dto.EstudianteResponse;
import pe.edu.upeu.bomerp.participacion.estudiante.service.EstudianteService;
import pe.edu.upeu.bomerp.participacion.estudiante.service.ExportacionOdooService;
import pe.edu.upeu.bomerp.participacion.estudiante.service.RankingService;
import pe.edu.upeu.bomerp.participacion.estudiante.dto.RankingResponse;
import pe.edu.upeu.bomerp.sedes.Sede;
import pe.edu.upeu.bomerp.sedes.SedeService;
import org.springframework.security.core.Authentication;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "Estudiantes")
@RestController
@RequestMapping("/api/v1/participacion/estudiantes")
@RequiredArgsConstructor
public class EstudianteController {

    private final EstudianteService estudianteService;
    private final ExportacionOdooService exportacionOdooService;
    private final RankingService rankingService;
    private final SedeService sedeService;

    @Operation(summary = "Devuelve el estudiante del usuario que inicio sesion, enlazandolo la primera vez")
    @GetMapping("/yo")
    public ResponseEntity<EstudianteResponse> yo(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(estudianteService.resolver(jwt));
    }

    @Operation(summary = "Lista los estudiantes registrados")
    @GetMapping
    public ResponseEntity<List<EstudianteResponse>> listar(@RequestParam(required = false) Sede sede,
                                                           @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(estudianteService.listar(sedeService.alcance(jwt, sede).orElse(null)));
    }

    @Operation(summary = "Ranking de la campania activa de una sede por puntos validados")
    @GetMapping("/ranking")
    public ResponseEntity<RankingResponse> ranking(@RequestParam(required = false) Sede sede,
                                                   @RequestParam(required = false) Long campaniaId,
                                                   @RequestParam(defaultValue = "10") int limite,
                                                   @AuthenticationPrincipal Jwt jwt,
                                                   Authentication autenticacion) {
        boolean esEstudiante = autenticacion.getAuthorities().stream()
                .anyMatch(rol -> rol.getAuthority().equals("ROLE_ESTUDIANTE"));
        Long propio = esEstudiante ? estudianteService.resolver(jwt).getId() : null;
        return ResponseEntity.ok(rankingService.ranking(sedeService.requerida(jwt, sede),
                esEstudiante ? null : campaniaId, propio, Math.max(1, Math.min(limite, 50))));
    }

    @Operation(summary = "CSV de estudiantes listo para importar como contactos en Odoo")
    @GetMapping("/exportar-odoo/contactos")
    public ResponseEntity<byte[]> contactosOdoo(@RequestParam(required = false) Sede sede,
                                                @AuthenticationPrincipal Jwt jwt) {
        return csv("estudiantes_odoo.csv",
                exportacionOdooService.contactos(sedeService.alcance(jwt, sede).orElse(null)));
    }

    @Operation(summary = "CSV de puntos validados por estudiante para las tarjetas de lealtad de Odoo")
    @GetMapping("/exportar-odoo/puntos")
    public ResponseEntity<byte[]> puntosOdoo(@RequestParam(required = false) Sede sede,
                                             @AuthenticationPrincipal Jwt jwt) {
        return csv("puntos_odoo.csv", exportacionOdooService.puntos(sedeService.alcance(jwt, sede).orElse(null)));
    }

    private ResponseEntity<byte[]> csv(String nombre, String contenido) {
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] cuerpo = contenido.getBytes(StandardCharsets.UTF_8);
        byte[] archivo = new byte[bom.length + cuerpo.length];
        System.arraycopy(bom, 0, archivo, 0, bom.length);
        System.arraycopy(cuerpo, 0, archivo, bom.length, cuerpo.length);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(archivo);
    }
}
