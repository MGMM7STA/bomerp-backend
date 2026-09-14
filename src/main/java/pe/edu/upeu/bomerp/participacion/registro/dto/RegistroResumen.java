package pe.edu.upeu.bomerp.participacion.registro.dto;

import lombok.Getter;
import pe.edu.upeu.bomerp.participacion.registro.entity.EstadoRegistro;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class RegistroResumen {
    private final Long id;
    private final Long estudianteId;
    private final LocalDateTime fecha;
    private final String estado;
    private final Integer totalEvidencias;
    private final BigDecimal puntajeTotal;
    private final long cantidadDetalles;

    public RegistroResumen(Long id, Long estudianteId, LocalDateTime fecha, EstadoRegistro estado,
                           Integer totalEvidencias, BigDecimal puntajeTotal, long cantidadDetalles) {
        this.id = id;
        this.estudianteId = estudianteId;
        this.fecha = fecha;
        this.estado = estado.name();
        this.totalEvidencias = totalEvidencias;
        this.puntajeTotal = puntajeTotal;
        this.cantidadDetalles = cantidadDetalles;
    }
}
