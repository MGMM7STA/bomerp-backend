package pe.edu.upeu.bomerp.participacion.registro.service;

import pe.edu.upeu.bomerp.sedes.Sede;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaResponse;
import pe.edu.upeu.bomerp.campanias.campania.service.CampaniaService;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.campanias.habito.service.HabitoService;
import pe.edu.upeu.bomerp.exception.EstadoRegistroInvalidoException;
import pe.edu.upeu.bomerp.exception.RegistroYaAnuladoException;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import pe.edu.upeu.bomerp.participacion.registro.dto.DetalleEvidenciaRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.FotoEvidencia;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroAgregado;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroReporte;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResponse;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResumen;
import pe.edu.upeu.bomerp.participacion.registro.entity.DetalleEvidencia;
import pe.edu.upeu.bomerp.participacion.registro.entity.EstadoRegistro;
import pe.edu.upeu.bomerp.participacion.registro.entity.RegistroParticipacion;
import pe.edu.upeu.bomerp.participacion.registro.mapper.RegistroMapper;
import pe.edu.upeu.bomerp.participacion.registro.repository.RegistroRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegistroServiceImpl implements RegistroService {

    private static final Set<String> CAMPOS_ORDENABLES =
            Set.of("fecha", "puntajeTotal", "totalEvidencias", "estudianteId", "id");

    private final RegistroRepository registroRepository;
    private final HabitoService habitoService;
    private final CampaniaService campaniaService;
    private final RegistroMapper registroMapper;
    private final FotoEvidenciaService fotoEvidenciaService;

    @Override
    @Transactional(readOnly = true)
    public List<RegistroResponse> listar() {
        return registroRepository.findAll().stream().map(registroMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroResponse> buscar(EstadoRegistro estado, Long estudianteId,
                                         LocalDateTime desde, LocalDateTime hasta,
                                         String ordenarPor, String direccion, Sede sede) {
        Sort sort = construirOrden(ordenarPor, direccion);
        return registroRepository.buscar(estado, estudianteId, desde, hasta, sede, sort)
                .stream().map(registroMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroReporte reporte(EstadoRegistro estado, Long estudianteId,
                                   LocalDateTime desde, LocalDateTime hasta, Sede sede) {
        RegistroAgregado agregado = registroRepository.agregados(estado, estudianteId, desde, hasta, sede);
        Sort sort = Sort.by(Sort.Direction.DESC, "fecha");
        List<RegistroResumen> registros =
                registroRepository.buscarResumen(estado, estudianteId, desde, hasta, sede, sort);
        return new RegistroReporte(agregado, registros);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroResponse obtener(Long id) {
        RegistroParticipacion registro = registroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado: " + id));
        return registroMapper.toResponse(registro);
    }

    @Override
    @Transactional
    public RegistroResponse crear(RegistroRequest request, Long estudianteId, Sede sede) {
        return crear(request, estudianteId, sede, Map.of());
    }

    @Override
    @Transactional
    public RegistroResponse crear(RegistroRequest request, Long estudianteId, Sede sede,
                                  Map<Integer, MultipartFile> fotos) {
        fotos.values().forEach(fotoEvidenciaService::verificar);
        CampaniaResponse campania = campaniaService.obtenerActiva(sede);
        LocalDate hoy = LocalDate.now();

        RegistroParticipacion registro = new RegistroParticipacion();
        registro.setEstudianteId(estudianteId);
        registro.setCampaniaId(campania.getId());
        registro.setFecha(LocalDateTime.now());
        registro.setEstado(EstadoRegistro.ENVIADO);
        registro.setSede(sede);

        BigDecimal puntajeTotal = BigDecimal.ZERO;
        int totalEvidencias = 0;

        for (int indice = 0; indice < request.getDetalles().size(); indice++) {
            DetalleEvidenciaRequest detalleRequest = request.getDetalles().get(indice);
            HabitoResponse habito = habitoService.obtener(detalleRequest.getHabitoId());
            if (!campaniaService.incluyeHabito(campania.getId(), habito.getId())) {
                throw new IllegalArgumentException(
                        "El habito '" + habito.getNombre() + "' no forma parte de la campania "
                                + campania.getNombre());
            }

            campaniaService.consumirCupo(
                    campania.getId(), estudianteId, hoy, detalleRequest.getVeces());

            DetalleEvidencia detalle = registroMapper.toDetalle(detalleRequest, habito);
            detalle.setRegistro(registro);
            detalle.setEstado(EstadoRegistro.ENVIADO);
            registro.getDetalles().add(detalle);

            puntajeTotal = puntajeTotal.add(detalle.getSubtotal());
            totalEvidencias += detalleRequest.getVeces();
        }

        registro.setPuntajeTotal(puntajeTotal);
        registro.setTotalEvidencias(totalEvidencias);

        for (int indice = 0; indice < registro.getDetalles().size(); indice++) {
            MultipartFile foto = fotos.get(indice);
            if (foto != null) {
                registro.getDetalles().get(indice).setArchivoImagen(fotoEvidenciaService.guardar(foto));
            }
        }

        return registroMapper.toResponse(registroRepository.save(registro));
    }

    @Override
    @Transactional
    public RegistroResponse anular(Long id) {
        RegistroParticipacion registro = registroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado: " + id));

        if (registro.getEstado() == EstadoRegistro.ANULADO) {
            throw new RegistroYaAnuladoException("El registro " + id + " ya esta anulado");
        }

        LocalDate fechaRegistro = registro.getFecha().toLocalDate();
        for (DetalleEvidencia detalle : registro.getDetalles()) {
            if (detalle.getEstado() != EstadoRegistro.RECHAZADO) {
                campaniaService.devolverCupo(
                        registro.getCampaniaId(),
                        registro.getEstudianteId(),
                        fechaRegistro,
                        detalle.getVeces());
                detalle.setEstado(EstadoRegistro.ANULADO);
            }
        }

        registro.setEstado(EstadoRegistro.ANULADO);
        return registroMapper.toResponse(registroRepository.save(registro));
    }

    @Override
    @Transactional
    public RegistroResponse validar(Long id, String revisor) {
        RegistroParticipacion registro = registroPorRevisar(id);
        for (DetalleEvidencia detalle : pendientes(registro)) {
            aprobar(detalle);
        }
        return cerrarRevision(registro, revisor);
    }

    @Override
    @Transactional
    public RegistroResponse rechazar(Long id, String motivo, String revisor) {
        RegistroParticipacion registro = registroPorRevisar(id);
        for (DetalleEvidencia detalle : pendientes(registro)) {
            descartar(registro, detalle, motivo);
        }
        return cerrarRevision(registro, revisor);
    }

    @Override
    @Transactional
    public RegistroResponse validarDetalle(Long registroId, Long detalleId, String revisor) {
        RegistroParticipacion registro = registroPorRevisar(registroId);
        aprobar(detallePorRevisar(registro, detalleId));
        return cerrarRevision(registro, revisor);
    }

    @Override
    @Transactional
    public RegistroResponse rechazarDetalle(Long registroId, Long detalleId, String motivo, String revisor) {
        RegistroParticipacion registro = registroPorRevisar(registroId);
        descartar(registro, detallePorRevisar(registro, detalleId), motivo);
        return cerrarRevision(registro, revisor);
    }

    @Override
    @Transactional(readOnly = true)
    public FotoEvidencia foto(Long registroId, Long detalleId) {
        RegistroParticipacion registro = registroRepository.findById(registroId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado: " + registroId));
        DetalleEvidencia detalle = buscarDetalle(registro, detalleId);
        if (detalle.getArchivoImagen() == null) {
            throw new ResourceNotFoundException("La evidencia " + detalleId + " no tiene foto");
        }
        return fotoEvidenciaService.leer(detalle.getArchivoImagen());
    }

    private RegistroParticipacion registroPorRevisar(Long id) {
        RegistroParticipacion registro = registroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado: " + id));
        if (registro.getEstado() != EstadoRegistro.ENVIADO) {
            throw new EstadoRegistroInvalidoException(
                    "El registro " + id + " ya fue revisado: su estado es " + registro.getEstado());
        }
        return registro;
    }

    private DetalleEvidencia buscarDetalle(RegistroParticipacion registro, Long detalleId) {
        return registro.getDetalles().stream()
                .filter(detalle -> detalle.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La evidencia " + detalleId + " no pertenece al registro " + registro.getId()));
    }

    private DetalleEvidencia detallePorRevisar(RegistroParticipacion registro, Long detalleId) {
        DetalleEvidencia detalle = buscarDetalle(registro, detalleId);
        if (detalle.getEstado() != EstadoRegistro.ENVIADO) {
            throw new EstadoRegistroInvalidoException(
                    "La evidencia " + detalleId + " ya fue revisada: su estado es " + detalle.getEstado());
        }
        return detalle;
    }

    private List<DetalleEvidencia> pendientes(RegistroParticipacion registro) {
        return registro.getDetalles().stream()
                .filter(detalle -> detalle.getEstado() == EstadoRegistro.ENVIADO)
                .toList();
    }

    private void aprobar(DetalleEvidencia detalle) {
        detalle.setEstado(EstadoRegistro.VALIDADO);
        detalle.setMotivoRechazo(null);
    }

    private void descartar(RegistroParticipacion registro, DetalleEvidencia detalle, String motivo) {
        campaniaService.liberarCupo(
                registro.getCampaniaId(),
                registro.getEstudianteId(),
                registro.getFecha().toLocalDate(),
                detalle.getVeces());
        detalle.setEstado(EstadoRegistro.RECHAZADO);
        detalle.setMotivoRechazo(motivo.trim());
    }

    private RegistroResponse cerrarRevision(RegistroParticipacion registro, String revisor) {
        boolean quedanPendientes = registro.getDetalles().stream()
                .anyMatch(detalle -> detalle.getEstado() == EstadoRegistro.ENVIADO);
        if (!quedanPendientes) {
            boolean algunoValido = registro.getDetalles().stream()
                    .anyMatch(detalle -> detalle.getEstado() == EstadoRegistro.VALIDADO);
            registro.setEstado(algunoValido ? EstadoRegistro.VALIDADO : EstadoRegistro.RECHAZADO);
            registro.setMotivoRechazo(algunoValido ? null : registro.getDetalles().stream()
                    .map(DetalleEvidencia::getMotivoRechazo)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null));
            registro.setRevisadoPor(revisor);
            registro.setFechaRevision(LocalDateTime.now());
        }
        return registroMapper.toResponse(registroRepository.save(registro));
    }

    private Sort construirOrden(String ordenarPor, String direccion) {
        if (!CAMPOS_ORDENABLES.contains(ordenarPor)) {
            throw new IllegalArgumentException(
                    "Campo de ordenamiento no permitido: '" + ordenarPor
                            + "'. Campos validos: " + CAMPOS_ORDENABLES);
        }
        Sort.Direction dir = "ASC".equalsIgnoreCase(direccion) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, ordenarPor);
    }
}
