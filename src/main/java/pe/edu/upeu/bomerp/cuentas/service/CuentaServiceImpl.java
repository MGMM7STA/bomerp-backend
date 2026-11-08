package pe.edu.upeu.bomerp.cuentas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.bomerp.cuentas.dto.CuentaRequest;
import pe.edu.upeu.bomerp.cuentas.dto.CuentaResponse;
import pe.edu.upeu.bomerp.participacion.estudiante.service.EstudianteService;
import pe.edu.upeu.bomerp.sedes.Sede;
import pe.edu.upeu.bomerp.sedes.SedeService;
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
    private final SedeService sedeService;
    private final EstudianteService estudianteService;

    @Override
    public List<CuentaResponse> listar() {
        Map<String, String> rolPorUsuario = new HashMap<>();
        for (String rol : ROLES.reversed()) {
            for (Map<String, Object> usuario : keycloak.usuariosConRol(rol)) {
                rolPorUsuario.put(texto(usuario, "id"), rol);
            }
        }
        Map<String, Sede> sedes = sedeService.todas();
        return keycloak.usuarios().stream()
                .filter(usuario -> !texto(usuario, "username").startsWith("service-account-"))
                .map(usuario -> aResponse(usuario, rolPorUsuario.get(texto(usuario, "id")),
                        sedes.get(texto(usuario, "username"))))
                .sorted(Comparator.comparing(CuentaResponse::getApellidos, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    @Override
    public CuentaResponse crear(CuentaRequest request) {
        if (!"ADMIN".equals(request.getRol()) && request.getSede() == null) {
            throw new IllegalArgumentException("Elige la sede de la cuenta: solo el administrador no tiene sede");
        }
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
            if (!"ADMIN".equals(request.getRol())) {
                sedeService.asignar(request.getUsuario().trim(), request.getSede());
            }
        } catch (RuntimeException e) {
            keycloak.eliminarUsuario(id);
            throw e;
        }
        log.info("Cuenta creada usuario={} rol={}", request.getUsuario(), request.getRol());
        return obtener(id);
    }

    @Override
    public CuentaResponse cambiarRol(String id, String rol, Sede sede, String solicitante) {
        if (id.equals(solicitante) && !"ADMIN".equals(rol)) {
            throw new IllegalArgumentException("No puedes quitarte el rol de administrador a ti mismo");
        }
        String nombreUsuario = texto(keycloak.usuario(id), "username");
        if ("ADMIN".equals(rol)) {
            sedeService.quitar(nombreUsuario);
        } else {
            Sede destino = sede != null ? sede : sedeService.de(nombreUsuario).orElseThrow(
                    () -> new IllegalArgumentException("Elige la sede de la cuenta"));
            sedeService.asignar(nombreUsuario, destino);
            estudianteService.cambiarSede(nombreUsuario, destino);
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

    @Override
    public void eliminar(String id) {
        keycloak.eliminarUsuario(id);
    }

    private CuentaResponse obtener(String id) {
        String rol = keycloak.rolesDe(id).stream()
                .map(r -> texto(r, "name"))
                .filter(ROLES::contains)
                .min(Comparator.comparingInt(ROLES::indexOf))
                .orElse(null);
        Map<String, Object> usuario = keycloak.usuario(id);
        return aResponse(usuario, rol, sedeService.de(texto(usuario, "username")).orElse(null));
    }

    private CuentaResponse aResponse(Map<String, Object> usuario, String rol, Sede sede) {
        Object creado = usuario.get("createdTimestamp");
        return CuentaResponse.builder()
                .id(texto(usuario, "id"))
                .usuario(texto(usuario, "username"))
                .nombres(texto(usuario, "firstName"))
                .apellidos(texto(usuario, "lastName"))
                .correo(texto(usuario, "email"))
                .rol(rol)
                .sede(sede == null ? null : sede.name())
                .activa(Boolean.TRUE.equals(usuario.get("enabled")))
                .creadaEn(creado instanceof Number n ? Instant.ofEpochMilli(n.longValue()) : null)
                .build();
    }

    private String texto(Map<String, Object> mapa, String clave) {
        Object valor = mapa.get(clave);
        return valor == null ? "" : valor.toString();
    }
}
