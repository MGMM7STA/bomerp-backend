package pe.edu.upeu.bomerp.participacion.registro.dto;

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
public class DetalleEvidenciaResponse {
    private Long habitoId;
    private String nombreHabito;
    private String descripcion;
    private String urlImagen;
    private BigDecimal puntajeUnitario;
    private Integer veces;
    private BigDecimal subtotal;
}