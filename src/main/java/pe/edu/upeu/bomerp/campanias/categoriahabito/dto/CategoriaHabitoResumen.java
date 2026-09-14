package pe.edu.upeu.bomerp.campanias.categoriahabito.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaHabitoResumen {
    private Long id;
    private String nombre;
}
