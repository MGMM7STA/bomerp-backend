package pe.edu.upeu.bomerp.cuentas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.bomerp.cuentas.dto.ContraseniaRequest;
import pe.edu.upeu.bomerp.cuentas.dto.CuentaRequest;
import pe.edu.upeu.bomerp.cuentas.dto.CuentaResponse;
import pe.edu.upeu.bomerp.cuentas.dto.EstadoCuentaRequest;
import pe.edu.upeu.bomerp.cuentas.dto.RolRequest;
import pe.edu.upeu.bomerp.cuentas.service.CuentaService;
import java.util.List;

@Tag(name = "Cuentas")
@RestController
@RequestMapping("/api/v1/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    @Operation(summary = "Lista las cuentas del realm con su rol y estado")
    @GetMapping
    public ResponseEntity<List<CuentaResponse>> listar() {
        return ResponseEntity.ok(cuentaService.listar());
    }

    @Operation(summary = "Crea una cuenta con contrasenia temporal y su rol")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CuentaResponse crear(@Valid @RequestBody CuentaRequest request) {
        return cuentaService.crear(request);
    }

    @Operation(summary = "Cambia el rol de una cuenta")
    @PatchMapping("/{id}/rol")
    public ResponseEntity<CuentaResponse> cambiarRol(@PathVariable String id,
                                                     @Valid @RequestBody RolRequest request,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(cuentaService.cambiarRol(id, request.getRol(), request.getSede(), jwt.getSubject()));
    }

    @Operation(summary = "Activa o desactiva una cuenta sin borrarla")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<CuentaResponse> cambiarEstado(@PathVariable String id,
                                                        @Valid @RequestBody EstadoCuentaRequest request,
                                                        @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(cuentaService.cambiarEstado(id, request.getActiva(), jwt.getSubject()));
    }

    @Operation(summary = "Pone una contrasenia temporal nueva")
    @PatchMapping("/{id}/contrasenia")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restablecerContrasenia(@PathVariable String id,
                                       @Valid @RequestBody ContraseniaRequest request) {
        cuentaService.restablecerContrasenia(id, request.getContrasenia());
    }
}
