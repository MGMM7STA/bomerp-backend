package pe.edu.upeu.bomerp.premios.premiacion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pe.edu.upeu.bomerp.premios.premiacion.dto.ActaPremiacionResponse;
import pe.edu.upeu.bomerp.premios.premiacion.dto.DetallePremiacionResponse;
import pe.edu.upeu.bomerp.premios.premiacion.entity.ActaPremiacion;
import pe.edu.upeu.bomerp.premios.premiacion.entity.DetallePremiacion;

@Mapper(componentModel = "spring")
public interface ActaPremiacionMapper {

    @Mapping(target = "detalles", source = "detalles")
    ActaPremiacionResponse toResponse(ActaPremiacion acta);

    DetallePremiacionResponse toDetalleResponse(
            DetallePremiacion detalle
    );
}