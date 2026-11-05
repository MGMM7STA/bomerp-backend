package pe.edu.upeu.bomerp.campanias.campania.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaRequest;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaResponse;
import pe.edu.upeu.bomerp.campanias.campania.entity.Campania;
import pe.edu.upeu.bomerp.campanias.campania.entity.CampaniaHabito;
import pe.edu.upeu.bomerp.campanias.campania.entity.CupoDiario;
import pe.edu.upeu.bomerp.campanias.campania.mapper.CampaniaMapper;
import pe.edu.upeu.bomerp.campanias.campania.repository.CampaniaHabitoRepository;
import pe.edu.upeu.bomerp.campanias.campania.repository.CampaniaRepository;
import pe.edu.upeu.bomerp.campanias.campania.repository.CupoDiarioRepository;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.campanias.habito.service.HabitoService;
import pe.edu.upeu.bomerp.exception.CupoDiarioExcedidoException;
import pe.edu.upeu.bomerp.exception.ReferenciaEnUsoException;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaniaServiceImpl implements CampaniaService {

    private static final String ACTIVA = "ACTIVA";

    private final CampaniaRepository campaniaRepository;
    private final CupoDiarioRepository cupoDiarioRepository;
    private final CampaniaMapper campaniaMapper;
    private final CampaniaHabitoRepository campaniaHabitoRepository;
    private final HabitoService habitoService;

    @Override
    @Transactional(readOnly = true)
    public List<CampaniaResponse> listar() {
        return campaniaRepository.findAll().stream().map(this::aResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CampaniaResponse obtener(Long id) {
        return aResponse(buscarOFallar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CampaniaResponse obtenerActiva() {
        Campania campania = campaniaRepository.findFirstByEstado(ACTIVA)
                .orElseThrow(() -> new ResourceNotFoundException("No hay ninguna campania activa"));
        return aResponse(campania);
    }

    @Override
    @Transactional
    public CampaniaResponse crear(CampaniaRequest request) {
        validarFechas(request);
        validarActivaUnica(request, null);
        Campania campania = campaniaMapper.toEntity(request);
        log.info("Creando campania nombre={} estado={}", request.getNombre(), request.getEstado());
        Campania guardada = campaniaRepository.save(campania);
        sincronizarHabitos(guardada.getId(), request.getHabitoIds());
        return aResponse(guardada);
    }

    @Override
    @Transactional
    public CampaniaResponse actualizar(Long id, CampaniaRequest request) {
        Campania campania = buscarOFallar(id);
        validarFechas(request);
        validarActivaUnica(request, id);
        campaniaMapper.actualizar(request, campania);
        log.info("Actualizando campania id={} estado={}", id, request.getEstado());
        Campania guardada = campaniaRepository.save(campania);
        sincronizarHabitos(guardada.getId(), request.getHabitoIds());
        return aResponse(guardada);
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

    @Override
    @Transactional
    public void liberarCupo(Long campaniaId, Long estudianteId, LocalDate fecha, Integer veces) {
        cupoDiarioRepository.findByCampaniaIdAndEstudianteIdAndFecha(campaniaId, estudianteId, fecha)
                .ifPresent(cupo -> {
                    cupo.setEvidenciasUsadas(Math.max(0, cupo.getEvidenciasUsadas() - veces));
                    cupoDiarioRepository.save(cupo);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<HabitoResponse> habitosDeActiva() {
        Campania activa = campaniaRepository.findFirstByEstado(ACTIVA)
                .orElseThrow(() -> new ResourceNotFoundException("No hay ninguna campania activa"));
        return campaniaHabitoRepository.findByCampaniaId(activa.getId()).stream()
                .map(enlace -> habitoService.obtener(enlace.getHabitoId()))
                .filter(habito -> Integer.valueOf(1).equals(habito.getActivo()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean incluyeHabito(Long campaniaId, Long habitoId) {
        return campaniaHabitoRepository.existsByCampaniaIdAndHabitoId(campaniaId, habitoId);
    }

    private void sincronizarHabitos(Long campaniaId, List<Long> habitoIds) {
        if (habitoIds == null) {
            return;
        }
        campaniaHabitoRepository.deleteByCampaniaId(campaniaId);
        campaniaHabitoRepository.flush();
        for (Long habitoId : new LinkedHashSet<>(habitoIds)) {
            habitoService.obtener(habitoId);
            CampaniaHabito enlace = new CampaniaHabito();
            enlace.setCampaniaId(campaniaId);
            enlace.setHabitoId(habitoId);
            campaniaHabitoRepository.save(enlace);
        }
    }

    private CampaniaResponse aResponse(Campania campania) {
        CampaniaResponse respuesta = campaniaMapper.toResponse(campania);
        respuesta.setHabitoIds(campaniaHabitoRepository.findByCampaniaId(campania.getId()).stream()
                .map(CampaniaHabito::getHabitoId)
                .toList());
        return respuesta;
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

    @Override
    @Transactional
    public void devolverCupo(Long campaniaId, Long estudianteId, LocalDate fecha, Integer veces) {
        CupoDiario cupo = cupoDiarioRepository
                .findByCampaniaIdAndEstudianteIdAndFecha(campaniaId, estudianteId, fecha)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe cupo diario registrado para la campania " + campaniaId
                                + ", estudiante " + estudianteId + " y fecha " + fecha));

        int usadas = Math.max(0, cupo.getEvidenciasUsadas() - veces);
        log.info("Devolviendo cupo campania={} estudiante={} fecha={} veces={} usadas={} -> {}",
                campaniaId, estudianteId, fecha, veces, cupo.getEvidenciasUsadas(), usadas);
        cupo.setEvidenciasUsadas(usadas);
        cupoDiarioRepository.save(cupo);
    }
}
