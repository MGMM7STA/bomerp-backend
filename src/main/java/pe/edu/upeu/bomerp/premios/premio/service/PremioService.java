package pe.edu.upeu.bomerp.premios.premio.service;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.bomerp.premios.categoria.entity.CategoriaPremio;
import pe.edu.upeu.bomerp.premios.categoria.repository.CategoriaPremioRepository;
import pe.edu.upeu.bomerp.premios.premio.dto.PremioRequest;
import pe.edu.upeu.bomerp.premios.premio.dto.PremioResponse;
import pe.edu.upeu.bomerp.premios.premio.entity.Premio;
import pe.edu.upeu.bomerp.premios.premio.mapper.PremioMapper;
import pe.edu.upeu.bomerp.premios.premio.repository.PremioRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PremioService {

    private final PremioRepository repository;
    private final PremioMapper mapper;
    private final CategoriaPremioRepository categoriaRepository;

    @Transactional(readOnly = true)
        public List<PremioResponse> listar(Long categoriaId) {

        List<Premio> premios;

        if (categoriaId != null) {
                premios = repository.findByCategoriaIdConCategoria(categoriaId);
        } else {
                premios = repository.findAllConCategoria();
        }

        return premios.stream()
                .map(mapper::toResponse)
                .toList();
        }       

    @Transactional(readOnly = true)
public PremioResponse obtener(Long id) {
    Premio premio = repository.findByIdConCategoria(id)
            .orElseThrow(() ->
                new RuntimeException("Premio no encontrado con ID: " + id)
            );

    return mapper.toResponse(premio);
}

    public PremioResponse crear(PremioRequest request) {

        CategoriaPremio categoria = categoriaRepository
                .findById(request.getCategoriaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoría de premio no encontrada"
                ));

        Premio premio = mapper.toEntity(request);

        premio.setCategoria(categoria);

        Premio guardado = repository.save(premio);

        return mapper.toResponse(guardado);
    }

    

@Transactional
public PremioResponse actualizar(
        Long id,
        PremioRequest request) {

    Premio premio = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Premio no encontrado"
            ));

    CategoriaPremio categoria = categoriaRepository
            .findById(request.getCategoriaId())
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Categoría de premio no encontrada"
            ));

    premio.setNombre(request.getNombre());
    premio.setDescripcion(request.getDescripcion());
    premio.setStock(request.getStock());
    premio.setActivo(request.getActivo());
    premio.setCategoria(categoria);

    Premio actualizado = repository.save(premio);

    return mapper.toResponse(actualizado);
}


    public void eliminar(Long id) {

        Premio premio = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Premio no encontrado"
                ));

        repository.delete(premio);
    }
    public void descontarStock(Long premioId, Integer cantidad) {

    Premio premio = repository.findById(premioId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Premio no encontrado"
            ));

    if (cantidad == null || cantidad <= 0) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La cantidad debe ser mayor a cero"
        );
    }

    if (premio.getStock() < cantidad) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Stock insuficiente para el premio"
        );
    }

    premio.setStock(premio.getStock() - cantidad);

    repository.save(premio);
}


public void aumentarStock(Long premioId, Integer cantidad) {

    Premio premio = repository.findById(premioId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Premio no encontrado"
            ));

    if (cantidad == null || cantidad <= 0) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La cantidad debe ser mayor a cero"
        );
    }

    premio.setStock(premio.getStock() + cantidad);

    repository.save(premio);
}

}