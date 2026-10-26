package pe.edu.upeu.bomerp.premios.premio.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pe.edu.upeu.bomerp.premios.premio.dto.PremioRequest;
import pe.edu.upeu.bomerp.premios.premio.dto.PremioResponse;
import pe.edu.upeu.bomerp.premios.premio.entity.Premio;

@Mapper(componentModel = "spring")
public interface PremioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    Premio toEntity(PremioRequest request);

    PremioResponse toResponse(Premio premio);
}