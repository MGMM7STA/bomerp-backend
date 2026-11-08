package pe.edu.upeu.bomerp.participacion.estudiante.service;

import pe.edu.upeu.bomerp.participacion.estudiante.dto.RankingResponse;
import pe.edu.upeu.bomerp.sedes.Sede;

public interface RankingService {

    RankingResponse ranking(Sede sede, Long campaniaId, Long estudianteId, int limite);
}
