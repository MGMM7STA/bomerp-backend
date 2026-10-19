package pe.edu.upeu.bomerp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "Autenticacion")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Operation(summary = "Devuelve el perfil del usuario autenticado leido del token")
    @GetMapping("/perfil")
    public ResponseEntity<PerfilResponse> perfil(@AuthenticationPrincipal Jwt jwt) {
        log.info("Consultando perfil de sub={}", jwt.getSubject());
        return ResponseEntity.ok(new PerfilResponse(
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("name"),
                rolesDelToken(jwt)));
    }

    private List<String> rolesDelToken(Jwt jwt) {
        List<String> roles = new ArrayList<>();
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return roles;
        }
        Object valores = realmAccess.get("roles");
        if (!(valores instanceof Collection<?> lista)) {
            return roles;
        }
        for (Object rol : lista) {
            roles.add(rol.toString());
        }
        return roles;
    }
}
