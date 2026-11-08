package pe.edu.upeu.bomerp.premios.premio.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import pe.edu.upeu.bomerp.auditoria.AuditoriaService;

import pe.edu.upeu.bomerp.premios.premio.dto.PremioRequest;
import pe.edu.upeu.bomerp.premios.premio.dto.PremioResponse;
import pe.edu.upeu.bomerp.premios.premio.entity.Premio;
import pe.edu.upeu.bomerp.premios.premio.service.PremioService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
@Tag(name = "Premios", description = "Operaciones enfocadas en el catálogo de premios")
@RestController
@RequestMapping("/api/v1/premios")
@RequiredArgsConstructor
public class PremioController {
    public interface PremioRepository extends JpaRepository<Premio, Long> {

    @Query("""
        SELECT p
        FROM Premio p
        JOIN FETCH p.categoria
        """)
    List<Premio> listarConCategoria();
}

    private final PremioService service;
    private final AuditoriaService auditoria;

    @GetMapping
    public List<PremioResponse> listar(
            @RequestParam(required = false) Long categoriaId) {

        return service.listar(categoriaId);
    }

    @GetMapping("/{id}")
    public PremioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PremioResponse crear(
            @Valid @RequestBody PremioRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        PremioResponse premio = service.crear(request);
        auditoria.registrar(jwt, "CREAR_PREMIO", "PREMIO", premio.getId(), resumen(premio), null);
        return premio;
    }

    @PutMapping("/{id}")
    public PremioResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PremioRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        PremioResponse antes = service.obtener(id);
        PremioResponse premio = service.actualizar(id, request);
        auditoria.registrar(jwt, "EDITAR_PREMIO", "PREMIO", id, cambios(antes, premio), null);
        return premio;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        PremioResponse premio = service.obtener(id);
        service.eliminar(id);
        auditoria.registrar(jwt, "ELIMINAR_PREMIO", "PREMIO", id, resumen(premio), null);
    }

    private String resumen(PremioResponse premio) {
        return premio.getNombre() + " - stock " + premio.getStock() + " - " + categoria(premio);
    }

    private String cambios(PremioResponse antes, PremioResponse despues) {
        List<String> cambios = new ArrayList<>();
        if (!Objects.equals(antes.getNombre(), despues.getNombre())) {
            cambios.add("Nombre de " + antes.getNombre() + " a " + despues.getNombre());
        }
        if (!Objects.equals(antes.getStock(), despues.getStock())) {
            cambios.add("Stock de " + antes.getStock() + " a " + despues.getStock());
        }
        if (!Objects.equals(categoria(antes), categoria(despues))) {
            cambios.add("Categoria de " + categoria(antes) + " a " + categoria(despues));
        }
        if (!Objects.equals(antes.getActivo(), despues.getActivo())) {
            cambios.add(Boolean.TRUE.equals(despues.getActivo()) ? "Activado" : "Desactivado");
        }
        if (!Objects.equals(antes.getDescripcion(), despues.getDescripcion())) {
            cambios.add("Descripcion actualizada");
        }
        String detalle = cambios.isEmpty() ? "Sin cambios" : String.join("; ", cambios);
        return despues.getNombre() + ": " + detalle;
    }

    private String categoria(PremioResponse premio) {
        return premio.getCategoria() == null ? "-" : premio.getCategoria().nombre();
    }
}
