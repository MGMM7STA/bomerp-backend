package pe.edu.upeu.bomerp.premios.premiacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import pe.edu.upeu.bomerp.premios.premiacion.entity.EstadoActaPremiacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActaPremiacionResponse {

    private Long id;
    private Long campaniaId;
    private LocalDate fechaInicioPeriodo;
    private LocalDate fechaFinPeriodo;
    private LocalDateTime fechaEmision;
    private EstadoActaPremiacion estado;
    private Integer totalGanadores;
    private Integer totalPremiosEntregados;
    private List<DetallePremiacionResponse> detalles;
}