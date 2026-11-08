package pe.edu.upeu.bomerp.premios.premiacion.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import pe.edu.upeu.bomerp.premios.premiacion.dto.ActaPremiacionResponse;
import pe.edu.upeu.bomerp.premios.premiacion.dto.ActaPremiacionRequest;
import pe.edu.upeu.bomerp.premios.premiacion.service.ActaPremiacionService;
import pe.edu.upeu.bomerp.sedes.Sede;
import pe.edu.upeu.bomerp.sedes.SedeService;
import pe.edu.upeu.bomerp.auditoria.AuditoriaService;

import java.util.List;

@Tag(name = "Actas de premiación", description = "Emisión y consulta de actas de premiación")
@RestController
@RequestMapping("/api/v1/actas-premiacion")
@RequiredArgsConstructor
public class ActaPremiacionController {

    private final ActaPremiacionService service;
    private final SedeService sedeService;
    private final AuditoriaService auditoria;

    @GetMapping
    public List<ActaPremiacionResponse> listar(@RequestParam(required = false) Sede sede,
                                               @AuthenticationPrincipal Jwt jwt) {
        return service.listar(sedeService.alcance(jwt, sede).orElse(null));
    }

    @GetMapping("/{id}")
    public ActaPremiacionResponse obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.obtener(id, sedeService.alcance(jwt, null).orElse(null));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActaPremiacionResponse emitir(@Valid @RequestBody ActaPremiacionRequest request,
                                         @RequestParam(required = false) Sede sede,
                                         @AuthenticationPrincipal Jwt jwt) {
        ActaPremiacionResponse acta = service.emitir(request, sedeService.requerida(jwt, sede));
        auditoria.registrar(jwt, "EMITIR_ACTA", "ACTA_PREMIACION", acta.getId(),
                acta.getTotalGanadores() + " ganadores, " + acta.getTotalPremiosEntregados()
                        + " premios descontados del stock", acta.getSede());
        return acta;
    }

    @PatchMapping("/{id}/anular")
    public ActaPremiacionResponse anular(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        ActaPremiacionResponse acta = service.anular(id, sedeService.alcance(jwt, null).orElse(null));
        auditoria.registrar(jwt, "ANULAR_ACTA", "ACTA_PREMIACION", acta.getId(),
                acta.getTotalPremiosEntregados() + " premios devueltos al stock", acta.getSede());
        return acta;
    }
}
