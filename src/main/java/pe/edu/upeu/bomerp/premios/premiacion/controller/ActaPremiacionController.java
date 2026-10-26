package pe.edu.upeu.bomerp.premios.premiacion.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import pe.edu.upeu.bomerp.premios.premiacion.dto.ActaPremiacionResponse;
import pe.edu.upeu.bomerp.premios.premiacion.dto.ActaPremiacionRequest;
import pe.edu.upeu.bomerp.premios.premiacion.service.ActaPremiacionService;

import java.util.List;

@Tag(name = "Actas de premiación", description = "Emisión y consulta de actas de premiación")
@RestController
@RequestMapping("/api/v1/actas-premiacion")
@RequiredArgsConstructor
public class ActaPremiacionController {

    private final ActaPremiacionService service;

    @GetMapping
    public List<ActaPremiacionResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ActaPremiacionResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActaPremiacionResponse emitir(@Valid @RequestBody ActaPremiacionRequest request) {
        return service.emitir(request);
    }

    @PatchMapping("/{id}/anular")
    public ActaPremiacionResponse anular(@PathVariable Long id) {
        return service.anular(id);
    }
}
