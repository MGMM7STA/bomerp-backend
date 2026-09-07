package pe.edu.upeu.bomerp.campanias.campania.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaResponse;
import pe.edu.upeu.bomerp.campanias.campania.entity.Campania;
import pe.edu.upeu.bomerp.campanias.campania.entity.CupoDiario;
import pe.edu.upeu.bomerp.campanias.campania.mapper.CampaniaMapper;
import pe.edu.upeu.bomerp.campanias.campania.repository.CampaniaRepository;
import pe.edu.upeu.bomerp.campanias.campania.repository.CupoDiarioRepository;
import pe.edu.upeu.bomerp.exception.CupoDiarioExcedidoException;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaniaServiceImpl implements CampaniaService {

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
        Campania campania = campaniaRepository.findFirstByEstado("ACTIVA")
                .orElseThrow(() -> new ResourceNotFoundException("No hay ninguna campania activa"));
        return campaniaMapper.toResponse(campania);
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

    private Campania buscarOFallar(Long id) {
        return campaniaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campania no encontrada: " + id));
    }
}