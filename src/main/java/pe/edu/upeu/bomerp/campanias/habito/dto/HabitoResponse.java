package pe.edu.upeu.bomerp.campanias.habito.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitoResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal puntajeBase;
    private Integer activo;
}