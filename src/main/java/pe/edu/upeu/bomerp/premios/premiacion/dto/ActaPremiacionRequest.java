package pe.edu.upeu.bomerp.premios.premiacion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ActaPremiacionRequest {

    @NotNull
    private Long campaniaId;

    @NotNull
    private LocalDate fechaInicioPeriodo;

    @NotNull
    private LocalDate fechaFinPeriodo;

    @NotEmpty
    @Valid
    private List<DetallePremiacionRequest> detalles;
}