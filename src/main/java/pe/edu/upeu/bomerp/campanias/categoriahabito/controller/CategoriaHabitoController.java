package pe.edu.upeu.bomerp.campanias.categoriahabito.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.bomerp.campanias.categoriahabito.dto.CategoriaHabitoRequest;
import pe.edu.upeu.bomerp.campanias.categoriahabito.dto.CategoriaHabitoResponse;
import pe.edu.upeu.bomerp.campanias.categoriahabito.service.CategoriaHabitoService;
import java.util.List;

@Slf4j
@Tag(name = "Categorias de habito")
@RestController
@RequestMapping("/api/v1/categorias-habito")
@RequiredArgsConstructor
public class CategoriaHabitoController {

    private final CategoriaHabitoService categoriaHabitoService;

    @Operation(summary = "Lista las categorias de habito")
    @GetMapping
    public ResponseEntity<List<CategoriaHabitoResponse>> listar() {
        return ResponseEntity.ok(categoriaHabitoService.listar());
    }

    @Operation(summary = "Consulta una categoria de habito por id")
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaHabitoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaHabitoService.obtener(id));
    }

    @Operation(summary = "Registra una categoria de habito")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaHabitoResponse crear(@Valid @RequestBody CategoriaHabitoRequest request) {
        return categoriaHabitoService.crear(request);
    }

    @Operation(summary = "Actualiza una categoria de habito")
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaHabitoResponse> actualizar(@PathVariable Long id,
                                                             @Valid @RequestBody CategoriaHabitoRequest request) {
        return ResponseEntity.ok(categoriaHabitoService.actualizar(id, request));
    }

    @Operation(summary = "Elimina una categoria de habito sin habitos asociados")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        categoriaHabitoService.eliminar(id);
    }
}
