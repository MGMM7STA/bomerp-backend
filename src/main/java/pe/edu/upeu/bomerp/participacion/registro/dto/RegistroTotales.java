package pe.edu.upeu.bomerp.participacion.registro.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record RegistroTotales(
        long totalRegistros,
        long evidenciasTotales,
        BigDecimal puntajeValidado,
        BigDecimal puntajePromedio,
        String habitoMasRegistrado,
        long vecesHabito) {

    public static RegistroTotales de(long totalRegistros, long evidenciasTotales, BigDecimal puntajeValidado,
                                     String habito, long veces) {
        BigDecimal promedio = totalRegistros == 0
                ? BigDecimal.ZERO
                : puntajeValidado.divide(BigDecimal.valueOf(totalRegistros), 2, RoundingMode.HALF_UP);
        return new RegistroTotales(totalRegistros, evidenciasTotales, puntajeValidado, promedio, habito, veces);
    }
}
