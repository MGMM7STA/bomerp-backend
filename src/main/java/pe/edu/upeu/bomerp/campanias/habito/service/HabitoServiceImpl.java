package pe.edu.upeu.bomerp.campanias.habito.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.campanias.habito.entity.Habito;
import pe.edu.upeu.bomerp.campanias.habito.mapper.HabitoMapper;
import pe.edu.upeu.bomerp.campanias.habito.repository.HabitoRepository;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitoServiceImpl implements HabitoService {

    private final HabitoRepository habitoRepository;
    private final HabitoMapper habitoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HabitoResponse> listar() {
        return habitoRepository.findAll().stream().map(habitoMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HabitoResponse obtener(Long id) {
        Habito habito = habitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habito no encontrado: " + id));
        return habitoMapper.toResponse(habito);
    }
}