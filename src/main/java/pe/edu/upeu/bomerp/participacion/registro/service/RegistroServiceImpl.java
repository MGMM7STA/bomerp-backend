package pe.edu.upeu.bomerp.participacion.registro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaResponse;
import pe.edu.upeu.bomerp.campanias.campania.service.CampaniaService;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.campanias.habito.service.HabitoService;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import pe.edu.upeu.bomerp.participacion.registro.dto.DetalleEvidenciaRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResponse;
import pe.edu.upeu.bomerp.participacion.registro.entity.DetalleEvidencia;
import pe.edu.upeu.bomerp.participacion.registro.entity.EstadoRegistro;
import pe.edu.upeu.bomerp.participacion.registro.entity.RegistroParticipacion;
import pe.edu.upeu.bomerp.participacion.registro.mapper.RegistroMapper;
import pe.edu.upeu.bomerp.participacion.registro.repository.RegistroRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistroServiceImpl implements RegistroService {

    private final RegistroRepository registroRepository;
    private final HabitoService habitoService;
    private final CampaniaService campaniaService;
    private final RegistroMapper registroMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RegistroResponse> listar() {
        return registroRepository.findAll().stream().map(registroMapper::toResponse).toList();
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
    public RegistroResponse crear(RegistroRequest request) {
        CampaniaResponse campania = campaniaService.obtenerActiva();
        LocalDate hoy = LocalDate.now();

        RegistroParticipacion registro = new RegistroParticipacion();
        registro.setEstudianteId(request.getEstudianteId());
        registro.setCampaniaId(campania.getId());
        registro.setFecha(LocalDateTime.now());
        registro.setEstado(EstadoRegistro.ENVIADO);

        BigDecimal puntajeTotal = BigDecimal.ZERO;
        int totalEvidencias = 0;

        for (DetalleEvidenciaRequest detalleRequest : request.getDetalles()) {
            HabitoResponse habito = habitoService.obtener(detalleRequest.getHabitoId());

            campaniaService.consumirCupo(
                    campania.getId(), request.getEstudianteId(), hoy, detalleRequest.getVeces());

            DetalleEvidencia detalle = registroMapper.toDetalle(detalleRequest, habito);
            detalle.setRegistro(registro);
            registro.getDetalles().add(detalle);

            puntajeTotal = puntajeTotal.add(detalle.getSubtotal());
            totalEvidencias += detalleRequest.getVeces();
        }

        registro.setPuntajeTotal(puntajeTotal);
        registro.setTotalEvidencias(totalEvidencias);

        return registroMapper.toResponse(registroRepository.save(registro));
    }
}