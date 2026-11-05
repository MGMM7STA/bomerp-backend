package pe.edu.upeu.bomerp.participacion.registro.service;

import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.bomerp.participacion.registro.dto.FotoEvidencia;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroReporte;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResponse;
import pe.edu.upeu.bomerp.participacion.registro.entity.EstadoRegistro;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface RegistroService {

    List<RegistroResponse> listar();

    List<RegistroResponse> buscar(EstadoRegistro estado, Long estudianteId,
                                  LocalDateTime desde, LocalDateTime hasta,
                                  String ordenarPor, String direccion);

    RegistroReporte reporte(EstadoRegistro estado, Long estudianteId,
                            LocalDateTime desde, LocalDateTime hasta);

    RegistroResponse obtener(Long id);

    RegistroResponse crear(RegistroRequest request, Long estudianteId);

    RegistroResponse crear(RegistroRequest request, Long estudianteId, Map<Integer, MultipartFile> fotos);

    RegistroResponse anular(Long id);

    RegistroResponse validar(Long id, String revisor);

    RegistroResponse rechazar(Long id, String motivo, String revisor);

    RegistroResponse validarDetalle(Long registroId, Long detalleId, String revisor);

    RegistroResponse rechazarDetalle(Long registroId, Long detalleId, String motivo, String revisor);

    FotoEvidencia foto(Long registroId, Long detalleId);
}
