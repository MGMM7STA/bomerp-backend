package pe.edu.upeu.bomerp.premios.categoria.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
            @Valid @RequestBody CategoriaPremioRequest request) {

        return service.crear(request);
    }

    @PutMapping("/{id}")
    public CategoriaPremioResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaPremioRequest request) {

        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}