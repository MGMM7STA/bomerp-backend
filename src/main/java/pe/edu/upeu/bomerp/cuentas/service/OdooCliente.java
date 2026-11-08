package pe.edu.upeu.bomerp.cuentas.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import pe.edu.upeu.bomerp.exception.ServicioExternoException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class OdooCliente {

    private static final ParameterizedTypeReference<Map<String, Object>> OBJETO =
            new ParameterizedTypeReference<>() { };

    private final String url;
    private final String base;
    private final String login;
    private final String apiKey;
    private final AtomicInteger contador = new AtomicInteger();
    private Integer uid;

    public OdooCliente(@Value("${bomerp.odoo.url:}") String url,
                       @Value("${bomerp.odoo.db:}") String base,
                       @Value("${bomerp.odoo.login:}") String login,
                       @Value("${bomerp.odoo.api-key:}") String apiKey) {
        this.url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        this.base = base;
        this.login = login;
        this.apiKey = apiKey;
    }

    public boolean configurado() {
        return !url.isBlank() && !base.isBlank() && !login.isBlank() && !apiKey.isBlank();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> buscar(String modelo, List<Object> dominio, List<String> campos,
                                            String orden, Integer limite) {
        Map<String, Object> opciones = new HashMap<>();
        opciones.put("fields", campos);
        if (orden != null) {
            opciones.put("order", orden);
        }
        if (limite != null) {
            opciones.put("limit", limite);
        }
        return (List<Map<String, Object>>) ejecutar(modelo, "search_read", List.of(dominio), opciones);
    }

    public Map<String, Object> leer(String modelo, int id, List<String> campos) {
        List<Map<String, Object>> filas = buscar(modelo, List.of(List.of("id", "=", id)), campos, null, 1);
        if (filas.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe el registro " + id + " en Odoo (" + modelo + ")");
        }
        return filas.get(0);
    }

    public void escribir(String modelo, int id, Map<String, Object> valores) {
        ejecutar(modelo, "write", List.of(List.of(id), valores), Map.of());
    }

    public int crear(String modelo, Map<String, Object> valores) {
        Object resultado = ejecutar(modelo, "create", List.of(valores), Map.of());
        if (resultado instanceof List<?> lista && !lista.isEmpty()) {
            return ((Number) lista.get(0)).intValue();
        }
        return ((Number) resultado).intValue();
    }

    public void llamar(String modelo, String metodo, List<Object> argumentos, Map<String, Object> opciones) {
        ejecutar(modelo, metodo, argumentos, opciones);
    }

    public byte[] decodificar(Object base64) {
        if (!(base64 instanceof String texto) || texto.isBlank()) {
            return null;
        }
        return Base64.getMimeDecoder().decode(texto);
    }

    private Object ejecutar(String modelo, String metodo, List<Object> argumentos, Map<String, Object> opciones) {
        if (!configurado()) {
            throw new ServicioExternoException(
                    "Odoo no esta configurado: define ODOO_URL, ODOO_DB, ODOO_LOGIN y ODOO_API_KEY", null);
        }
        Object resultado = rpc("object", "execute_kw",
                List.of(base, usuario(), apiKey, modelo, metodo, argumentos, opciones));
        log.debug("Odoo {}.{} respondio", modelo, metodo);
        return resultado;
    }

    private synchronized int usuario() {
        if (uid == null) {
            Object respuesta = rpc("common", "authenticate", List.of(base, login, apiKey, Map.of()));
            if (!(respuesta instanceof Number numero)) {
                throw new ServicioExternoException(
                        "Odoo rechazo el usuario " + login + ": revisa ODOO_LOGIN y la API key", null);
            }
            uid = numero.intValue();
        }
        return uid;
    }

    @SuppressWarnings("unchecked")
    private Object rpc(String servicio, String metodo, List<Object> argumentos) {
        Map<String, Object> cuerpo = Map.of(
                "jsonrpc", "2.0",
                "method", "call",
                "id", contador.incrementAndGet(),
                "params", Map.of("service", servicio, "method", metodo, "args", argumentos));
        Map<String, Object> respuesta;
        try {
            respuesta = RestClient.create().post()
                    .uri(url + "/jsonrpc")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(OBJETO);
        } catch (RestClientException e) {
            throw new ServicioExternoException("No se pudo conectar con Odoo en " + url
                    + ". Revisa que la instancia de AWS este encendida y que ODOO_URL tenga el DNS actual", e);
        }
        if (respuesta == null) {
            throw new ServicioExternoException("Odoo no respondio", null);
        }
        if (respuesta.get("error") instanceof Map<?, ?> error) {
            Object datos = error.get("data");
            String detalle = datos instanceof Map<?, ?> d && d.get("message") != null
                    ? d.get("message").toString()
                    : String.valueOf(error.get("message"));
            if (detalle.contains("Access Denied") || detalle.contains("AccessDenied")) {
                uid = null;
            }
            throw new ServicioExternoException("Odoo respondio con error: " + detalle, null);
        }
        return respuesta.get("result");
    }
}
