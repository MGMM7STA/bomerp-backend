package pe.edu.upeu.bomerp.campanias.campania.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaRequest;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaResponse;
import pe.edu.upeu.bomerp.campanias.campania.entity.Campania;
import pe.edu.upeu.bomerp.campanias.campania.entity.CupoDiario;
import pe.edu.upeu.bomerp.campanias.campania.mapper.CampaniaMapper;
import pe.edu.upeu.bomerp.campanias.campania.repository.CampaniaRepository;
import pe.edu.upeu.bomerp.campanias.campania.repository.CupoDiarioRepository;
import pe.edu.upeu.bomerp.exception.CupoDiarioExcedidoException;
import pe.edu.upeu.bomerp.exception.ReferenciaEnUsoException;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaniaServiceImpl implements CampaniaService {

    private static final String ACTIVA = "ACTIVA";

    private final CampaniaRepository campaniaRepository;
    private final CupoDiarioRepository cupoDiarioRepository;
    private final CampaniaMapper campaniaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CampaniaResponse> listar() {
        return campaniaRepository.findAll().stream().map(campaniaMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CampaniaResponse obtener(Long id) {
        return campaniaMapper.toResponse(buscarOFallar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CampaniaResponse obtenerActiva() {
        Campania campania = campaniaRepository.findFirstByEstado(ACTIVA)
                .orElseThrow(() -> new ResourceNotFoundException("No hay ninguna campania activa"));
        return campaniaMapper.toResponse(campania);
    }

    @Override
    @Transactional
    public CampaniaResponse crear(CampaniaRequest request) {
        validarFechas(request);
        validarActivaUnica(request, null);
        Campania campania = campaniaMapper.toEntity(request);
        log.info("Creando campania nombre={} estado={}", request.getNombre(), request.getEstado());
        return campaniaMapper.toResponse(campaniaRepository.save(campania));
    }

    @Override
    @Transactional
    public CampaniaResponse actualizar(Long id, CampaniaRequest request) {
        Campania campania = buscarOFallar(id);
        validarFechas(request);
        validarActivaUnica(request, id);
        campaniaMapper.actualizar(request, campania);
        log.info("Actualizando campania id={} estado={}", id, request.getEstado());
        return campaniaMapper.toResponse(campaniaRepository.save(campania));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Campania campania = buscarOFallar(id);
        long cuposUsados = cupoDiarioRepository.countByCampaniaId(id);
        if (cuposUsados > 0) {
            throw new ReferenciaEnUsoException(
                    "No se puede eliminar la campania '" + campania.getNombre()
                            + "': ya tiene " + cuposUsados + " dia(s) con participacion registrada. Cierrela en lugar de borrarla.");
        }
        log.info("Eliminando campania id={}", id);
        campaniaRepository.delete(campania);
    }

    @Override
    @Transactional
    public void consumirCupo(Long campaniaId, Long estudianteId, LocalDate fecha, Integer veces) {
        Campania campania = buscarOFallar(campaniaId);

        CupoDiario cupo = cupoDiarioRepository
                .findByCampaniaIdAndEstudianteIdAndFecha(campaniaId, estudianteId, fecha)
                .orElseGet(() -> {
                    CupoDiario nuevo = new CupoDiario();
                    nuevo.setCampaniaId(campaniaId);
                    nuevo.setEstudianteId(estudianteId);
                    nuevo.setFecha(fecha);
                    nuevo.setEvidenciasUsadas(0);
                    return nuevo;
                });

        int usadas = cupo.getEvidenciasUsadas() + veces;
        if (usadas > campania.getMaxEvidenciasDia()) {
            throw new CupoDiarioExcedidoException(
                    "Cupo diario excedido: la campania " + campania.getNombre()
                            + " permite " + campania.getMaxEvidenciasDia()
                            + " evidencias por dia; el estudiante " + estudianteId
                            + " ya uso " + cupo.getEvidenciasUsadas()
                            + " y esta intentando registrar " + veces + " mas");
        }
        cupo.setEvidenciasUsadas(usadas);
        cupoDiarioRepository.save(cupo);
    }

    private void validarFechas(CampaniaRequest request) {
        if (!request.getFechaFin().isAfter(request.getFechaInicio())) {
            throw new IllegalArgumentException(
                    "La fecha de fin (" + request.getFechaFin() + ") debe ser posterior a la de inicio ("
                            + request.getFechaInicio() + ")");
        }
    }

    private void validarActivaUnica(CampaniaRequest request, Long idActual) {
        if (!ACTIVA.equals(request.getEstado())) {
            return;
        }
        campaniaRepository.findFirstByEstado(ACTIVA)
                .filter(otra -> idActual == null || !otra.getId().equals(idActual))
                .ifPresent(otra -> {
                    throw new ReferenciaEnUsoException(
                            "Ya existe una campania ACTIVA: '" + otra.getNombre()
                                    + "' (id " + otra.getId() + "). Cierrela antes de activar otra.");
                });
    }

    private Campania buscarOFallar(Long id) {
        return campaniaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campania no encontrada: " + id));
    }
}
