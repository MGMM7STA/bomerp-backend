package pe.edu.upeu.bomerp.campanias.habito.mapper;

import org.mapstruct.Mapper;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.campanias.habito.entity.Habito;

@Mapper(componentModel = "spring")
public interface HabitoMapper {
    HabitoResponse toResponse(Habito habito);
}