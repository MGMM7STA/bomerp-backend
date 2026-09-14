package pe.edu.upeu.bomerp.campanias.campania.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaRequest;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaResponse;
import pe.edu.upeu.bomerp.campanias.campania.entity.Campania;

@Mapper(componentModel = "spring")
public interface CampaniaMapper {

    CampaniaResponse toResponse(Campania campania);

    @Mapping(target = "id", ignore = true)
    Campania toEntity(CampaniaRequest request);

    @Mapping(target = "id", ignore = true)
    void actualizar(CampaniaRequest request, @MappingTarget Campania campania);
}
