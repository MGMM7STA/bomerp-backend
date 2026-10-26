package pe.edu.upeu.bomerp.premios.categoria.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaPremioRequest {

    @NotBlank
    @Size(max = 80)
    private String nombre;

    @Size(max = 200)
    private String descripcion;
}