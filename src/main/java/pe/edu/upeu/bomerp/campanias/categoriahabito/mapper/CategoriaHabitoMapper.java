package pe.edu.upeu.bomerp.campanias.categoriahabito.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pe.edu.upeu.bomerp.campanias.categoriahabito.dto.CategoriaHabitoRequest;
import pe.edu.upeu.bomerp.campanias.categoriahabito.dto.CategoriaHabitoResponse;
import pe.edu.upeu.bomerp.campanias.categoriahabito.dto.CategoriaHabitoResumen;
import pe.edu.upeu.bomerp.campanias.categoriahabito.entity.CategoriaHabito;

@Mapper(componentModel = "spring")
public interface CategoriaHabitoMapper {

    CategoriaHabitoResponse toResponse(CategoriaHabito categoria);

    CategoriaHabitoResumen toResumen(CategoriaHabito categoria);

    @Mapping(target = "id", ignore = true)
    CategoriaHabito toEntity(CategoriaHabitoRequest request);

    @Mapping(target = "id", ignore = true)
    void actualizar(CategoriaHabitoRequest request, @MappingTarget CategoriaHabito categoria);
}
