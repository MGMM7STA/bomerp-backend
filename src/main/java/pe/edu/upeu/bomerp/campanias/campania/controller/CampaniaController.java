package pe.edu.upeu.bomerp.campanias.campania.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaResponse;
import pe.edu.upeu.bomerp.campanias.campania.service.CampaniaService;
import java.util.List;

@Tag(name = "Campanias")
@RestController
@RequestMapping("/api/v1/campanias")
@RequiredArgsConstructor
public class CampaniaController {

    private final CampaniaService campaniaService;

    @Operation(summary = "Lista las campanias")
    @GetMapping
    public ResponseEntity<List<CampaniaResponse>> listar() {
        return ResponseEntity.ok(campaniaService.listar());
    }

    @Operation(summary = "Devuelve la campania activa con su maximo de evidencias por dia")
    @GetMapping("/activa")
    public ResponseEntity<CampaniaResponse> activa() {
        return ResponseEntity.ok(campaniaService.obtenerActiva());
    }

    @Operation(summary = "Consulta una campania por id")
    @GetMapping("/{id}")
    public ResponseEntity<CampaniaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(campaniaService.obtener(id));
    }
}