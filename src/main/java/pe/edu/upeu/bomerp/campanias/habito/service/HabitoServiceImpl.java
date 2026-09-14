package pe.edu.upeu.bomerp.campanias.habito.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.campanias.categoriahabito.entity.CategoriaHabito;
import pe.edu.upeu.bomerp.campanias.categoriahabito.service.CategoriaHabitoService;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoRequest;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.campanias.habito.entity.Habito;
import pe.edu.upeu.bomerp.campanias.habito.mapper.HabitoMapper;
import pe.edu.upeu.bomerp.campanias.habito.repository.HabitoRepository;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitoServiceImpl implements HabitoService {

    private final HabitoRepository habitoRepository;
    private final CategoriaHabitoService categoriaHabitoService;
    private final HabitoMapper habitoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HabitoResponse> listar(Long categoriaId) {
        List<Habito> habitos = (categoriaId == null)
                ? habitoRepository.findAll()
                : habitoRepository.findByCategoriaId(categoriaId);
        return habitos.stream().map(habitoMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HabitoResponse obtener(Long id) {
        return habitoMapper.toResponse(buscarOFallar(id));
    }

    @Override
    @Transactional
    public HabitoResponse crear(HabitoRequest request) {
        CategoriaHabito categoria = categoriaHabitoService.referenciaObligatoria(request.getCategoriaId());
        Habito habito = habitoMapper.toEntity(request);
        habito.setCategoria(categoria);
        log.info("Creando habito nombre={} categoriaId={}", request.getNombre(), request.getCategoriaId());
        return habitoMapper.toResponse(habitoRepository.save(habito));
    }

    @Override
    @Transactional
    public HabitoResponse actualizar(Long id, HabitoRequest request) {
        Habito habito = buscarOFallar(id);
        CategoriaHabito categoria = categoriaHabitoService.referenciaObligatoria(request.getCategoriaId());
        habitoMapper.actualizar(request, habito);
        habito.setCategoria(categoria);
        log.info("Actualizando habito id={} categoriaId={}", id, request.getCategoriaId());
        return habitoMapper.toResponse(habitoRepository.save(habito));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Habito habito = buscarOFallar(id);
        log.info("Eliminando habito id={}", id);
        habitoRepository.delete(habito);
    }

    private Habito buscarOFallar(Long id) {
        return habitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habito no encontrado: " + id));
    }
}
