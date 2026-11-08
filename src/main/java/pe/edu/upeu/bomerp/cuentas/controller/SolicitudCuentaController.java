package pe.edu.upeu.bomerp.cuentas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.bomerp.cuentas.dto.AprobacionRequest;
import pe.edu.upeu.bomerp.cuentas.dto.AprobacionResponse;
import pe.edu.upeu.bomerp.cuentas.dto.ArchivoCarne;
import pe.edu.upeu.bomerp.cuentas.dto.RechazoSolicitudRequest;
import pe.edu.upeu.bomerp.cuentas.dto.SolicitudCuentaResponse;
import pe.edu.upeu.bomerp.cuentas.entity.EstadoSolicitud;
import pe.edu.upeu.bomerp.cuentas.service.SolicitudCuentaService;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Tag(name = "Solicitudes de cuenta")
@RestController
@RequestMapping("/api/v1/solicitudes-cuenta")
@RequiredArgsConstructor
public class SolicitudCuentaController {

    private final SolicitudCuentaService solicitudService;

    @Operation(summary = "Lista las solicitudes: las pendientes se leen en vivo de los leads de Odoo")
    @GetMapping
    public ResponseEntity<List<SolicitudCuentaResponse>> listar(
            @RequestParam(required = false) EstadoSolicitud estado) {
        return ResponseEntity.ok(solicitudService.listar(estado));
    }

    @Operation(summary = "Devuelve la foto del carne adjunta al lead de Odoo")
    @GetMapping("/{id}/carne")
    public ResponseEntity<Resource> carne(@PathVariable Long id) {
        ArchivoCarne carne = solicitudService.carne(id);
        return ResponseEntity.ok()
                .contentType(carne.tipo())
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
                .body(carne.contenido());
    }

    @Operation(summary = "Aprueba el lead: crea la cuenta y el estudiante con su sede, marca el lead en Odoo y envia el correo desde Odoo")
    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<AprobacionResponse> aprobar(@PathVariable Long id,
                                                      @Valid @RequestBody AprobacionRequest request,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(solicitudService.aprobar(id, request, nombreDe(jwt)));
    }

    @Operation(summary = "Rechaza el lead con un motivo y avisa al estudiante por correo desde Odoo")
    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<SolicitudCuentaResponse> rechazar(@PathVariable Long id,
                                                            @Valid @RequestBody RechazoSolicitudRequest request,
                                                            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(solicitudService.rechazar(id, request.getMotivo(), nombreDe(jwt)));
    }

    private String nombreDe(Jwt jwt) {
        String nombre = jwt.getClaimAsString("name");
        return nombre != null ? nombre : jwt.getClaimAsString("preferred_username");
    }
}
