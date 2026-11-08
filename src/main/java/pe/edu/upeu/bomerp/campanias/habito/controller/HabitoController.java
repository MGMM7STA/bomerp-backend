package pe.edu.upeu.bomerp.campanias.habito.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import pe.edu.upeu.bomerp.auditoria.AuditoriaService;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoRequest;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.campanias.habito.service.HabitoService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Tag(name = "Habitos")
@RestController
@RequestMapping("/api/v1/habitos")
@RequiredArgsConstructor
public class HabitoController {

    private final HabitoService habitoService;
    private final AuditoriaService auditoria;

    @Operation(summary = "Lista los habitos, opcionalmente filtrados por categoria")
    @GetMapping
    public ResponseEntity<List<HabitoResponse>> listar(
            @RequestParam(required = false) Long categoriaId) {
        log.info("Consultando habitos categoriaId={}", categoriaId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().cachePrivate())
                .body(habitoService.listar(categoriaId));
    }

    @Operation(summary = "Consulta un habito por id, con su categoria")
    @GetMapping("/{id}")
    public ResponseEntity<HabitoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(habitoService.obtener(id));
    }

    @Operation(summary = "Registra un habito validando que la categoria exista")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HabitoResponse crear(@Valid @RequestBody HabitoRequest request, @AuthenticationPrincipal Jwt jwt) {
        HabitoResponse habito = habitoService.crear(request);
        auditoria.registrar(jwt, "CREAR_HABITO", "HABITO", habito.getId(), resumen(habito), null);
        return habito;
    }

    @Operation(summary = "Actualiza un habito")
    @PutMapping("/{id}")
    public ResponseEntity<HabitoResponse> actualizar(@PathVariable Long id,
                                                     @Valid @RequestBody HabitoRequest request,
                                                     @AuthenticationPrincipal Jwt jwt) {
        HabitoResponse antes = habitoService.obtener(id);
        HabitoResponse habito = habitoService.actualizar(id, request);
        auditoria.registrar(jwt, "EDITAR_HABITO", "HABITO", id, cambios(antes, habito), null);
        return ResponseEntity.ok(habito);
    }

    @Operation(summary = "Elimina un habito")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        HabitoResponse habito = habitoService.obtener(id);
        habitoService.eliminar(id);
        auditoria.registrar(jwt, "ELIMINAR_HABITO", "HABITO", id, resumen(habito), null);
    }

    private String resumen(HabitoResponse habito) {
        return habito.getNombre() + " - " + puntaje(habito.getPuntajeBase()) + " pts - " + categoria(habito);
    }

    private String cambios(HabitoResponse antes, HabitoResponse despues) {
        List<String> cambios = new ArrayList<>();
        if (!Objects.equals(antes.getNombre(), despues.getNombre())) {
            cambios.add("Nombre de " + antes.getNombre() + " a " + despues.getNombre());
        }
        if (!puntaje(antes.getPuntajeBase()).equals(puntaje(despues.getPuntajeBase()))) {
            cambios.add("Puntaje de " + puntaje(antes.getPuntajeBase()) + " a " + puntaje(despues.getPuntajeBase()));
        }
        if (!Objects.equals(categoria(antes), categoria(despues))) {
            cambios.add("Categoria de " + categoria(antes) + " a " + categoria(despues));
        }
        if (!Objects.equals(antes.getActivo(), despues.getActivo())) {
            cambios.add(Integer.valueOf(1).equals(despues.getActivo()) ? "Activado" : "Desactivado");
        }
        if (!Objects.equals(antes.getDescripcion(), despues.getDescripcion())) {
            cambios.add("Descripcion actualizada");
        }
        String detalle = cambios.isEmpty() ? "Sin cambios" : String.join("; ", cambios);
        return despues.getNombre() + ": " + detalle;
    }

    private String puntaje(BigDecimal valor) {
        return valor == null ? "-" : valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String categoria(HabitoResponse habito) {
        return habito.getCategoria() == null ? "-" : habito.getCategoria().getNombre();
    }
}
