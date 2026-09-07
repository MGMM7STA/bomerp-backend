package pe.edu.upeu.bomerp.campanias.campania.mapper;

import org.mapstruct.Mapper;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaResponse;
import pe.edu.upeu.bomerp.campanias.campania.entity.Campania;

@Mapper(componentModel = "spring")
public interface CampaniaMapper {
    CampaniaResponse toResponse(Campania campania);
}