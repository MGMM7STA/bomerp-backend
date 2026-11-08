package pe.edu.upeu.bomerp.participacion.registro.service;

import pe.edu.upeu.bomerp.sedes.Sede;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.bomerp.participacion.registro.dto.FotoEvidencia;
import pe.edu.upeu.bomerp.participacion.registro.dto.PaginaResponse;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroTotales;
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
                                  String ordenarPor, String direccion, Sede sede);

    RegistroReporte reporte(EstadoRegistro estado, Long estudianteId,
                            LocalDateTime desde, LocalDateTime hasta, Sede sede);

    PaginaResponse<RegistroResponse> buscarPagina(EstadoRegistro estado, Long estudianteId,
                                                  LocalDateTime desde, LocalDateTime hasta,
                                                  String ordenarPor, String direccion, Sede sede,
                                                  int pagina, int tamanio);

    RegistroTotales totales(EstadoRegistro estado, Long estudianteId,
                            LocalDateTime desde, LocalDateTime hasta, Sede sede);

    RegistroResponse obtener(Long id);

    RegistroResponse crear(RegistroRequest request, Long estudianteId, Sede sede);

    RegistroResponse crear(RegistroRequest request, Long estudianteId, Sede sede, Map<Integer, MultipartFile> fotos);

    RegistroResponse anular(Long id);

    RegistroResponse validar(Long id, String revisor);

    RegistroResponse rechazar(Long id, String motivo, String revisor);

    RegistroResponse validarDetalle(Long registroId, Long detalleId, String revisor);

    RegistroResponse rechazarDetalle(Long registroId, Long detalleId, String motivo, String revisor);

    FotoEvidencia foto(Long registroId, Long detalleId);
}
