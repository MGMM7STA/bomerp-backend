package pe.edu.upeu.bomerp.participacion.registro.service;

import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroReporte;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResponse;
import pe.edu.upeu.bomerp.participacion.registro.entity.EstadoRegistro;
import java.time.LocalDateTime;
import java.util.List;

public interface RegistroService {

    List<RegistroResponse> listar();

    List<RegistroResponse> buscar(EstadoRegistro estado, Long estudianteId,
                                  LocalDateTime desde, LocalDateTime hasta,
                                  String ordenarPor, String direccion);

    RegistroReporte reporte(EstadoRegistro estado, Long estudianteId,
                            LocalDateTime desde, LocalDateTime hasta);

    RegistroResponse obtener(Long id);

    RegistroResponse crear(RegistroRequest request);
}
