package pe.edu.upeu.bomerp.premios.categoria.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import pe.edu.upeu.bomerp.auditoria.AuditoriaService;

import pe.edu.upeu.bomerp.premios.categoria.dto.CategoriaPremioRequest;
import pe.edu.upeu.bomerp.premios.categoria.dto.CategoriaPremioResponse;
import pe.edu.upeu.bomerp.premios.categoria.service.CategoriaPremioService;
import java.util.List;


@Tag(name = "Categoría de Premios", description = "Operaciones enfocadas en la gestión de categorías") 
@RestController
@RequestMapping("/api/v1/categorias-premio")
@RequiredArgsConstructor
public class CategoriaPremioController {

    private final CategoriaPremioService service;
    private final AuditoriaService auditoria;

    @GetMapping
    public List<CategoriaPremioResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public CategoriaPremioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaPremioResponse crear(
            @Valid @RequestBody CategoriaPremioRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        CategoriaPremioResponse categoria = service.crear(request);
        auditoria.registrar(jwt, "CREAR_CATEGORIA_PREMIO", "CATEGORIA_PREMIO", categoria.getId(),
                categoria.getNombre(), null);
        return categoria;
    }

    @PutMapping("/{id}")
    public CategoriaPremioResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaPremioRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        CategoriaPremioResponse antes = service.obtener(id);
        CategoriaPremioResponse categoria = service.actualizar(id, request);
        auditoria.registrar(jwt, "EDITAR_CATEGORIA_PREMIO", "CATEGORIA_PREMIO", id,
                antes.getNombre().equals(categoria.getNombre())
                        ? categoria.getNombre() + ": descripcion actualizada"
                        : "Nombre de " + antes.getNombre() + " a " + categoria.getNombre(), null);
        return categoria;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        CategoriaPremioResponse categoria = service.obtener(id);
        service.eliminar(id);
        auditoria.registrar(jwt, "ELIMINAR_CATEGORIA_PREMIO", "CATEGORIA_PREMIO", id, categoria.getNombre(), null);
    }
}
