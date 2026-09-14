package pe.edu.upeu.bomerp.campanias.categoriahabito.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.campanias.categoriahabito.dto.CategoriaHabitoRequest;
import pe.edu.upeu.bomerp.campanias.categoriahabito.dto.CategoriaHabitoResponse;
import pe.edu.upeu.bomerp.campanias.categoriahabito.entity.CategoriaHabito;
import pe.edu.upeu.bomerp.campanias.categoriahabito.mapper.CategoriaHabitoMapper;
import pe.edu.upeu.bomerp.campanias.categoriahabito.repository.CategoriaHabitoRepository;
import pe.edu.upeu.bomerp.campanias.habito.repository.HabitoRepository;
import pe.edu.upeu.bomerp.exception.ReferenciaEnUsoException;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaHabitoServiceImpl implements CategoriaHabitoService {

    private final CategoriaHabitoRepository categoriaHabitoRepository;
    private final HabitoRepository habitoRepository;
    private final CategoriaHabitoMapper categoriaHabitoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaHabitoResponse> listar() {
        return categoriaHabitoRepository.findAll().stream()
                .map(categoriaHabitoMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaHabitoResponse obtener(Long id) {
        return categoriaHabitoMapper.toResponse(referenciaObligatoria(id));
    }

    @Override
    @Transactional
    public CategoriaHabitoResponse crear(CategoriaHabitoRequest request) {
        categoriaHabitoRepository.findByNombreIgnoreCase(request.getNombre())
                .ifPresent(existente -> {
                    throw new ReferenciaEnUsoException(
                            "Ya existe una categoria de habito llamada '" + request.getNombre() + "'");
                });
        CategoriaHabito categoria = categoriaHabitoMapper.toEntity(request);
        log.info("Creando categoria de habito nombre={}", request.getNombre());
        return categoriaHabitoMapper.toResponse(categoriaHabitoRepository.save(categoria));
    }

    @Override
    @Transactional
    public CategoriaHabitoResponse actualizar(Long id, CategoriaHabitoRequest request) {
        CategoriaHabito categoria = referenciaObligatoria(id);
        categoriaHabitoRepository.findByNombreIgnoreCase(request.getNombre())
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> {
                    throw new ReferenciaEnUsoException(
                            "Ya existe otra categoria de habito llamada '" + request.getNombre() + "'");
                });
        categoriaHabitoMapper.actualizar(request, categoria);
        log.info("Actualizando categoria de habito id={}", id);
        return categoriaHabitoMapper.toResponse(categoriaHabitoRepository.save(categoria));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        CategoriaHabito categoria = referenciaObligatoria(id);
        long enUso = habitoRepository.countByCategoriaId(id);
        if (enUso > 0) {
            throw new ReferenciaEnUsoException(
                    "No se puede eliminar la categoria '" + categoria.getNombre()
                            + "': tiene " + enUso + " habito(s) asociado(s). Reasignelos o desactivelos primero.");
        }
        log.info("Eliminando categoria de habito id={}", id);
        categoriaHabitoRepository.delete(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaHabito referenciaObligatoria(Long id) {
        return categoriaHabitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria de habito no encontrada: " + id));
    }
}
