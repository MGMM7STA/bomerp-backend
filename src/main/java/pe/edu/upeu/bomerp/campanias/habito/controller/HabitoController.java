package pe.edu.upeu.bomerp.campanias.habito.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.campanias.habito.service.HabitoService;
import java.util.List;

@Tag(name = "Habitos")
@RestController
@RequestMapping("/api/v1/habitos")
@RequiredArgsConstructor
public class HabitoController {

    private final HabitoService habitoService;

    @Operation(summary = "Lista los habitos de la campania")
    @GetMapping
    public ResponseEntity<List<HabitoResponse>> listar() {
        return ResponseEntity.ok(habitoService.listar());
    }

    @Operation(summary = "Consulta un habito por id")
    @GetMapping("/{id}")
    public ResponseEntity<HabitoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(habitoService.obtener(id));
    }
}