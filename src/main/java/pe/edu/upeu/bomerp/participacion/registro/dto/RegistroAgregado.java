package pe.edu.upeu.bomerp.participacion.registro.dto;

import lombok.Getter;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class RegistroAgregado {
    private final long totalRegistros;
    private final long evidenciasTotales;
    private final BigDecimal puntajeTotal;
    private final BigDecimal puntajePromedio;

    public RegistroAgregado(long totalRegistros, long evidenciasTotales, BigDecimal puntajeTotal) {
        this.totalRegistros = totalRegistros;
        this.evidenciasTotales = evidenciasTotales;
        this.puntajeTotal = puntajeTotal;
        this.puntajePromedio = totalRegistros == 0
                ? BigDecimal.ZERO
                : puntajeTotal.divide(BigDecimal.valueOf(totalRegistros), 2, RoundingMode.HALF_UP);
    }
}
