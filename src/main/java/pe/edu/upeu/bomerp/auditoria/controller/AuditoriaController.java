package pe.edu.upeu.bomerp.auditoria.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.bomerp.auditoria.dto.PaginaEventos;
import pe.edu.upeu.bomerp.auditoria.internal.AuditoriaServiceImpl;
import java.util.List;

@Tag(name = "Auditoria")
@RestController
@RequestMapping("/api/v1/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaServiceImpl auditoriaService;

    @Operation(summary = "Consulta paginada de las acciones criticas registradas")
    @GetMapping
    public ResponseEntity<PaginaEventos> buscar(@RequestParam(required = false) String accion,
                                                @RequestParam(required = false) String usuario,
                                                @RequestParam(name = "filtroSede", required = false) String sede,
                                                @RequestParam(defaultValue = "0") int pagina,
                                                @RequestParam(defaultValue = "20") int tamanio) {
        return ResponseEntity.ok(auditoriaService.buscar(accion, usuario, sede, pagina, tamanio));
    }

    @Operation(summary = "Acciones distintas registradas, para el filtro")
    @GetMapping("/acciones")
    public ResponseEntity<List<String>> acciones() {
        return ResponseEntity.ok(auditoriaService.acciones());
    }
}
