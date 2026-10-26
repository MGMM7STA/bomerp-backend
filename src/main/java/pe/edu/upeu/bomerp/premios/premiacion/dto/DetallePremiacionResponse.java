package pe.edu.upeu.bomerp.premios.premiacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetallePremiacionResponse {

    private Long id;
    private Long estudianteId;
    private Integer posicion;
    private Long premioId;
    private Integer cantidad;
    private Integer puntajeAlcanzado;
}