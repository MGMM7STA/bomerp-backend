package pe.edu.upeu.bomerp.campanias.categoriahabito.service;

import pe.edu.upeu.bomerp.campanias.categoriahabito.dto.CategoriaHabitoRequest;
import pe.edu.upeu.bomerp.campanias.categoriahabito.dto.CategoriaHabitoResponse;
import pe.edu.upeu.bomerp.campanias.categoriahabito.entity.CategoriaHabito;
import java.util.List;

public interface CategoriaHabitoService {

    List<CategoriaHabitoResponse> listar();

    CategoriaHabitoResponse obtener(Long id);

    CategoriaHabitoResponse crear(CategoriaHabitoRequest request);

    CategoriaHabitoResponse actualizar(Long id, CategoriaHabitoRequest request);

    void eliminar(Long id);

    CategoriaHabito referenciaObligatoria(Long id);
}
