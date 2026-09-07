package pe.edu.upeu.bomerp.participacion.registro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroResponse {
    private Long id;
    private Long estudianteId;
    private Long campaniaId;
    private LocalDateTime fecha;
    private String estado;
    private Integer totalEvidencias;
    private BigDecimal puntajeTotal;
    private List<DetalleEvidenciaResponse> detalles;
}