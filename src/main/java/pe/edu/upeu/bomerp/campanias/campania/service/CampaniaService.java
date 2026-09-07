package pe.edu.upeu.bomerp.campanias.campania.service;

import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaResponse;
import java.time.LocalDate;
import java.util.List;

public interface CampaniaService {
    List<CampaniaResponse> listar();
    CampaniaResponse obtener(Long id);
    CampaniaResponse obtenerActiva();
    void consumirCupo(Long campaniaId, Long estudianteId, LocalDate fecha, Integer veces);
}