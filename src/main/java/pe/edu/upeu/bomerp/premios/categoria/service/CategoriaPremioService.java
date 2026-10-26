package pe.edu.upeu.bomerp.premios.categoria.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.bomerp.premios.categoria.dto.CategoriaPremioRequest;
import pe.edu.upeu.bomerp.premios.categoria.dto.CategoriaPremioResponse;
import pe.edu.upeu.bomerp.premios.categoria.entity.CategoriaPremio;
import pe.edu.upeu.bomerp.premios.categoria.mapper.CategoriaPremioMapper;
import pe.edu.upeu.bomerp.premios.categoria.repository.CategoriaPremioRepository;
import pe.edu.upeu.bomerp.premios.premio.repository.PremioRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaPremioService {

    private final PremioRepository premioRepository;
    private final CategoriaPremioRepository repository;
    private final CategoriaPremioMapper mapper;

    public List<CategoriaPremioResponse> listar() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public CategoriaPremioResponse obtener(Long id) {
        CategoriaPremio categoria = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoría de premio no encontrada"
                ));

        return mapper.toResponse(categoria);
    }

    public CategoriaPremioResponse crear(CategoriaPremioRequest request) {
        CategoriaPremio categoria = mapper.toEntity(request);

        CategoriaPremio guardada = repository.save(categoria);

        return mapper.toResponse(guardada);
    }

    public CategoriaPremioResponse actualizar(
            Long id,
            CategoriaPremioRequest request) {

        CategoriaPremio categoria = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoría de premio no encontrada"
                ));

        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());

        CategoriaPremio actualizada = repository.save(categoria);

        return mapper.toResponse(actualizada);
    }
    public void eliminar(Long id) {

    CategoriaPremio categoria = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Categoría de premio no encontrada"
            ));

    if (premioRepository.existsByCategoriaId(id)) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No se puede eliminar la categoría porque tiene premios asociados"
        );
    }

    repository.delete(categoria);
    }


}