package pe.edu.upeu.bomerp.campanias.habito.service;

import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoRequest;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import java.util.List;

public interface HabitoService {

    List<HabitoResponse> listar(Long categoriaId);

    HabitoResponse obtener(Long id);

    HabitoResponse crear(HabitoRequest request);

    HabitoResponse actualizar(Long id, HabitoRequest request);

    void eliminar(Long id);
}
