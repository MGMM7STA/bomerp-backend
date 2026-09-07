package pe.edu.upeu.bomerp.campanias.campania.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaniaResponse {
    private Long id;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer maxEvidenciasDia;
    private String estado;
}