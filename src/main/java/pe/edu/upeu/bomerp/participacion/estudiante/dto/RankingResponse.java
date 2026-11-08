package pe.edu.upeu.bomerp.participacion.estudiante.dto;

import java.math.BigDecimal;
import java.util.List;

public record RankingResponse(
        Long campaniaId,
        String campania,
        String sede,
        List<Fila> filas,
        Fila mia,
        int participantes) {

    public record Fila(int posicion, Long estudianteId, String nombre, String escuela, BigDecimal puntos,
                       long evidencias) {
    }
}
