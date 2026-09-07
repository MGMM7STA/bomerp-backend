package pe.edu.upeu.bomerp.participacion.registro.service;

import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResponse;
import java.util.List;

public interface RegistroService {
    List<RegistroResponse> listar();
    RegistroResponse obtener(Long id);
    RegistroResponse crear(RegistroRequest request);
}