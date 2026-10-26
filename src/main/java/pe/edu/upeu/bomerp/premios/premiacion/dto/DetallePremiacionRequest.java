package pe.edu.upeu.bomerp.premios.premiacion.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetallePremiacionRequest {

    @NotNull
    private Long estudianteId;

    @NotNull
    @Min(1)
    private Integer posicion;

    @NotNull
    private Long premioId;

    @NotNull
    @Min(1)
    private Integer cantidad;

    @NotNull
    @Min(0)
    private Integer puntajeAlcanzado;
}