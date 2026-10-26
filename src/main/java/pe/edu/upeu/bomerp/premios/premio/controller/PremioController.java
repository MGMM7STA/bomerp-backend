package pe.edu.upeu.bomerp.premios.premio.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import pe.edu.upeu.bomerp.premios.premio.dto.PremioRequest;
import pe.edu.upeu.bomerp.premios.premio.dto.PremioResponse;
import pe.edu.upeu.bomerp.premios.premio.entity.Premio;
import pe.edu.upeu.bomerp.premios.premio.service.PremioService;

import java.util.List;
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
            @Valid @RequestBody PremioRequest request) {

        return service.crear(request);
    }

    @PutMapping("/{id}")
    public PremioResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PremioRequest request) {

        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}