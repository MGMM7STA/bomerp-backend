package pe.edu.upeu.bomerp.cuentas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.bomerp.cuentas.dto.CuentaRequest;
import pe.edu.upeu.bomerp.cuentas.dto.CuentaResponse;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CuentaServiceImpl implements CuentaService {

    private static final List<String> ROLES = List.of("ADMIN", "COORDINADOR", "ESTUDIANTE");

    private final KeycloakAdminCliente keycloak;

    @Override
    public List<CuentaResponse> listar() {
        Map<String, String> rolPorUsuario = new HashMap<>();
        for (String rol : ROLES.reversed()) {
            for (Map<String, Object> usuario : keycloak.usuariosConRol(rol)) {
                rolPorUsuario.put(texto(usuario, "id"), rol);
            }
        }
        return keycloak.usuarios().stream()
                .filter(usuario -> !texto(usuario, "username").startsWith("service-account-"))
                .map(usuario -> aResponse(usuario, rolPorUsuario.get(texto(usuario, "id"))))
                .sorted(Comparator.comparing(CuentaResponse::getApellidos, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    @Override
    public CuentaResponse crear(CuentaRequest request) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("username", request.getUsuario().trim());
        usuario.put("firstName", request.getNombres().trim());
        usuario.put("lastName", request.getApellidos().trim());
        usuario.put("email", request.getCorreo().trim().toLowerCase());
        usuario.put("emailVerified", true);
        usuario.put("enabled", true);
        usuario.put("credentials", List.of(Map.of(
                "type", "password", "value", request.getContrasenia(), "temporary", true)));

        String id = keycloak.crearUsuario(usuario);
        try {
            keycloak.asignarRoles(id, List.of(keycloak.rol(request.getRol())));
        } catch (RuntimeException e) {
            keycloak.eliminarUsuario(id);
            throw e;
        }
        log.info("Cuenta creada usuario={} rol={}", request.getUsuario(), request.getRol());
        return obtener(id);
    }

    @Override
    public CuentaResponse cambiarRol(String id, String rol, String solicitante) {
        if (id.equals(solicitante) && !"ADMIN".equals(rol)) {
            throw new IllegalArgumentException("No puedes quitarte el rol de administrador a ti mismo");
        }
        List<Map<String, Object>> actuales = keycloak.rolesDe(id).stream()
                .filter(r -> ROLES.contains(texto(r, "name")))
                .toList();
        List<Map<String, Object>> sobran = actuales.stream()
                .filter(r -> !rol.equals(texto(r, "name")))
                .toList();
        if (!sobran.isEmpty()) {
            keycloak.quitarRoles(id, sobran);
        }
        if (actuales.stream().noneMatch(r -> rol.equals(texto(r, "name")))) {
            keycloak.asignarRoles(id, List.of(keycloak.rol(rol)));
        }
        log.info("Rol de la cuenta {} cambiado a {}", id, rol);
        return obtener(id);
    }

    @Override
    public CuentaResponse cambiarEstado(String id, boolean activa, String solicitante) {
        if (id.equals(solicitante) && !activa) {
            throw new IllegalArgumentException("No puedes desactivar tu propia cuenta");
        }
        keycloak.actualizarUsuario(id, Map.of("enabled", activa));
        log.info("Cuenta {} {}", id, activa ? "activada" : "desactivada");
        return obtener(id);
    }

    @Override
    public void restablecerContrasenia(String id, String contrasenia) {
        keycloak.restablecerContrasenia(id, contrasenia);
        log.info("Contrasenia temporal restablecida para la cuenta {}", id);
    }

    private CuentaResponse obtener(String id) {
        String rol = keycloak.rolesDe(id).stream()
                .map(r -> texto(r, "name"))
                .filter(ROLES::contains)
                .min(Comparator.comparingInt(ROLES::indexOf))
                .orElse(null);
        return aResponse(keycloak.usuario(id), rol);
    }

    private CuentaResponse aResponse(Map<String, Object> usuario, String rol) {
        Object creado = usuario.get("createdTimestamp");
        return CuentaResponse.builder()
                .id(texto(usuario, "id"))
                .usuario(texto(usuario, "username"))
                .nombres(texto(usuario, "firstName"))
                .apellidos(texto(usuario, "lastName"))
                .correo(texto(usuario, "email"))
                .rol(rol)
                .activa(Boolean.TRUE.equals(usuario.get("enabled")))
                .creadaEn(creado instanceof Number n ? Instant.ofEpochMilli(n.longValue()) : null)
                .build();
    }

    private String texto(Map<String, Object> mapa, String clave) {
        Object valor = mapa.get(clave);
        return valor == null ? "" : valor.toString();
    }
}
