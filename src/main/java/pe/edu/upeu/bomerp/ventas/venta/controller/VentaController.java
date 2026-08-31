package pe.edu.upeu.bomerp.ventas.venta.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bomerp.ventas.venta.dto.VentaRequest;
import pe.edu.upeu.bomerp.ventas.venta.dto.VentaResponse;
import pe.edu.upeu.bomerp.ventas.venta.service.VentaService;
import java.util.List;

@Tag(name = "Ventas")
@RestController
@RequestMapping("/api/v1/ventas")
@RequiredArgsConstructor
public class VentaController {
    private final VentaService ventaService;

    @Operation(summary = "Lista las ventas registradas")
    @GetMapping
    public ResponseEntity<List<VentaResponse>> listar() {
        return ResponseEntity.ok(ventaService.listar());
    }

    @Operation(summary = "Consulta una venta por id, con sus detalles")
    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.obtener(id));
    }

    @Operation(summary = "Registra una venta con sus detalles, descontando stock")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VentaResponse crear(@Valid @RequestBody VentaRequest request) {
        return ventaService.crear(request);
    }
}