package pe.edu.upeu.bomerp.premios.premio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import pe.edu.upeu.bomerp.premios.categoria.dto.CategoriaPremioResumen;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremioResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private Integer stock;
    private Boolean activo;
    private CategoriaPremioResumen categoria;
}
