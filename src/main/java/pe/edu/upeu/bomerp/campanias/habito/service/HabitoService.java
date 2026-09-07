package pe.edu.upeu.bomerp.campanias.habito.service;

import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import java.util.List;

public interface HabitoService {
    List<HabitoResponse> listar();
    HabitoResponse obtener(Long id);
}