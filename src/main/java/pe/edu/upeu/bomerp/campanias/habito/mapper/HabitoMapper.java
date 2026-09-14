package pe.edu.upeu.bomerp.campanias.habito.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pe.edu.upeu.bomerp.campanias.categoriahabito.mapper.CategoriaHabitoMapper;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoRequest;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.campanias.habito.entity.Habito;

@Mapper(componentModel = "spring", uses = CategoriaHabitoMapper.class)
public interface HabitoMapper {

    HabitoResponse toResponse(Habito habito);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    Habito toEntity(HabitoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    void actualizar(HabitoRequest request, @MappingTarget Habito habito);
}
