package pe.edu.upeu.bomerp.premios.categoria.mapper;

import org.mapstruct.Mapper;

import pe.edu.upeu.bomerp.premios.categoria.dto.CategoriaPremioRequest;
import pe.edu.upeu.bomerp.premios.categoria.dto.CategoriaPremioResponse;
import pe.edu.upeu.bomerp.premios.categoria.dto.CategoriaPremioResumen;
import pe.edu.upeu.bomerp.premios.categoria.entity.CategoriaPremio;

@Mapper(componentModel = "spring")
public interface CategoriaPremioMapper {

    CategoriaPremio toEntity(CategoriaPremioRequest request);

    CategoriaPremioResponse toResponse(CategoriaPremio categoriaPremio);

    CategoriaPremioResumen toResumen(CategoriaPremio categoriaPremio);
}