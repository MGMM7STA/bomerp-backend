package pe.edu.upeu.bomerp.cuentas.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import pe.edu.upeu.bomerp.exception.ReferenciaEnUsoException;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import pe.edu.upeu.bomerp.exception.ServicioExternoException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class KeycloakAdminCliente {

    private static final ParameterizedTypeReference<List<Map<String, Object>>> LISTA =
            new ParameterizedTypeReference<>() { };

    private static final ParameterizedTypeReference<Map<String, Object>> OBJETO =
            new ParameterizedTypeReference<>() { };

    private final RestClient cliente;
    private final String urlToken;
    private final String clientId;
    private final String clientSecret;

    private String token;
    private Instant tokenVence = Instant.EPOCH;

    public KeycloakAdminCliente(@Value("${bomerp.keycloak.admin.url}") String url,
                                @Value("${bomerp.keycloak.admin.realm}") String realm,
                                @Value("${bomerp.keycloak.admin.client-id}") String clientId,
                                @Value("${bomerp.keycloak.admin.client-secret}") String clientSecret) {
        this.cliente = RestClient.builder().baseUrl(url + "/admin/realms/" + realm).build();
        this.urlToken = url + "/realms/" + realm + "/protocol/openid-connect/token";
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public List<Map<String, Object>> usuarios() {
        return llamar(() -> cliente.get()
                .uri("/users?first=0&max=2000&briefRepresentation=false")
                .header("Authorization", bearer())
                .retrieve()
                .body(LISTA));
    }

    public Map<String, Object> usuario(String id) {
        return llamar(() -> cliente.get()
                .uri("/users/{id}", id)
                .header("Authorization", bearer())
                .retrieve()
                .body(OBJETO));
    }

    public List<Map<String, Object>> usuariosConRol(String rol) {
        return llamar(() -> cliente.get()
                .uri("/roles/{rol}/users?first=0&max=2000&briefRepresentation=true", rol)
                .header("Authorization", bearer())
                .retrieve()
                .body(LISTA));
    }

    public List<Map<String, Object>> rolesDe(String id) {
        return llamar(() -> cliente.get()
                .uri("/users/{id}/role-mappings/realm", id)
                .header("Authorization", bearer())
                .retrieve()
                .body(LISTA));
    }

    public String crearUsuario(Map<String, Object> usuario) {
        ResponseEntity<Void> respuesta = llamar(() -> cliente.post()
                .uri("/users")
                .header("Authorization", bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .body(usuario)
                .retrieve()
                .toBodilessEntity());
        URI ubicacion = respuesta.getHeaders().getLocation();
        if (ubicacion == null) {
            throw new ServicioExternoException("Keycloak no devolvio el id de la cuenta creada", null);
        }
        String ruta = ubicacion.getPath();
        return ruta.substring(ruta.lastIndexOf('/') + 1);
    }

    public void actualizarUsuario(String id, Map<String, Object> cambios) {
        llamar(() -> cliente.put()
                .uri("/users/{id}", id)
                .header("Authorization", bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .body(cambios)
                .retrieve()
                .toBodilessEntity());
    }

    public void eliminarUsuario(String id) {
        llamar(() -> cliente.delete()
                .uri("/users/{id}", id)
                .header("Authorization", bearer())
                .retrieve()
                .toBodilessEntity());
    }

    public void restablecerContrasenia(String id, String contrasenia) {
        llamar(() -> cliente.put()
                .uri("/users/{id}/reset-password", id)
                .header("Authorization", bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("type", "password", "value", contrasenia, "temporary", true))
                .retrieve()
                .toBodilessEntity());
    }

    public Map<String, Object> rol(String nombre) {
        return llamar(() -> cliente.get()
                .uri("/roles/{rol}", nombre)
                .header("Authorization", bearer())
                .retrieve()
                .body(OBJETO));
    }

    public void asignarRoles(String id, List<Map<String, Object>> roles) {
        llamar(() -> cliente.post()
                .uri("/users/{id}/role-mappings/realm", id)
                .header("Authorization", bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .body(roles)
                .retrieve()
                .toBodilessEntity());
    }

    public void quitarRoles(String id, List<Map<String, Object>> roles) {
        llamar(() -> cliente.method(HttpMethod.DELETE)
                .uri("/users/{id}/role-mappings/realm", id)
                .header("Authorization", bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .body(roles)
                .retrieve()
                .toBodilessEntity());
    }

    private synchronized String bearer() {
        if (token == null || Instant.now().isAfter(tokenVence)) {
            MultiValueMap<String, String> formulario = new LinkedMultiValueMap<>();
            formulario.add("grant_type", "client_credentials");
            formulario.add("client_id", clientId);
            formulario.add("client_secret", clientSecret);
            Map<String, Object> respuesta;
            try {
                respuesta = RestClient.create().post()
                        .uri(urlToken)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(formulario)
                        .retrieve()
                        .body(OBJETO);
            } catch (HttpClientErrorException e) {
                throw new ServicioExternoException(
                        "Keycloak rechazo al cliente " + clientId + ": revisa que exista y que su secreto sea el correcto", e);
            } catch (ResourceAccessException e) {
                throw new ServicioExternoException("No se pudo conectar con Keycloak", e);
            }
            if (respuesta == null || respuesta.get("access_token") == null) {
                throw new ServicioExternoException("Keycloak no devolvio un token para " + clientId, null);
            }
            token = respuesta.get("access_token").toString();
            long segundos = ((Number) respuesta.getOrDefault("expires_in", 60)).longValue();
            tokenVence = Instant.now().plusSeconds(Math.max(10, segundos - 30));
        }
        return "Bearer " + token;
    }

    private <T> T llamar(Supplier<T> peticion) {
        try {
            return peticion.get();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("La cuenta no existe en Keycloak");
        } catch (HttpClientErrorException.Conflict e) {
            throw new ReferenciaEnUsoException("Ya existe una cuenta con ese usuario o correo");
        } catch (HttpClientErrorException.BadRequest e) {
            throw new IllegalArgumentException("Keycloak no acepto los datos: " + mensajeDe(e));
        } catch (HttpClientErrorException.Unauthorized e) {
            token = null;
            throw new ServicioExternoException("Keycloak rechazo el token del backend, vuelve a intentar", e);
        } catch (HttpClientErrorException.Forbidden e) {
            throw new ServicioExternoException(
                    "El cliente " + clientId + " no tiene los roles manage-users, view-users y view-realm en Keycloak", e);
        } catch (ResourceAccessException e) {
            throw new ServicioExternoException("No se pudo conectar con Keycloak", e);
        }
    }

    private String mensajeDe(HttpClientErrorException e) {
        String cuerpo = e.getResponseBodyAsString();
        int inicio = cuerpo.indexOf("\"errorMessage\":\"");
        if (inicio < 0) {
            inicio = cuerpo.indexOf("\"error_description\":\"");
        }
        if (inicio < 0) {
            return cuerpo.isBlank() ? e.getStatusText() : cuerpo;
        }
        int desde = cuerpo.indexOf(':', inicio) + 2;
        int hasta = cuerpo.indexOf('"', desde);
        return hasta > desde ? cuerpo.substring(desde, hasta) : cuerpo;
    }
}
