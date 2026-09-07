package pe.edu.upeu.bomerp.participacion.registro.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.participacion.registro.dto.DetalleEvidenciaRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.DetalleEvidenciaResponse;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResponse;
import pe.edu.upeu.bomerp.participacion.registro.entity.DetalleEvidencia;
import pe.edu.upeu.bomerp.participacion.registro.entity.RegistroParticipacion;

@Mapper(componentModel = "spring")
public interface RegistroMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registro", ignore = true)
    @Mapping(target = "habitoId", source = "habito.id")
    @Mapping(target = "nombreHabito", source = "habito.nombre")
    @Mapping(target = "puntajeUnitario", source = "habito.puntajeBase")
    @Mapping(target = "descripcion", source = "request.descripcion")
    @Mapping(target = "urlImagen", source = "request.urlImagen")
    @Mapping(target = "veces", source = "request.veces")
    @Mapping(target = "subtotal", expression = "java(habito.getPuntajeBase().multiply(java.math.BigDecimal.valueOf(request.getVeces())))")
    DetalleEvidencia toDetalle(DetalleEvidenciaRequest request, HabitoResponse habito);

    RegistroResponse toResponse(RegistroParticipacion registro);

    DetalleEvidenciaResponse toDetalleResponse(DetalleEvidencia detalle);
}