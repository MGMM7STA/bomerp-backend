package pe.edu.upeu.bomerp.sedes.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.bomerp.sedes.SedeService;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Sedes")
@RestController
@RequestMapping("/api/v1/sedes")
@RequiredArgsConstructor
public class SedeController {

    private final SedeService sedeService;

    @Operation(summary = "Devuelve la sede del usuario que inicio sesion (vacia para el administrador)")
    @GetMapping("/yo")
    public ResponseEntity<Map<String, Object>> yo(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("usuario", jwt.getClaimAsString("preferred_username"));
        respuesta.put("sede", sedeService.de(jwt.getClaimAsString("preferred_username")).map(Enum::name).orElse(null));
        return ResponseEntity.ok(respuesta);
    }
}
