package pe.edu.upeu.bomerp.premios.premio.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PremioRequest {

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @Size(max = 200)
    private String descripcion;

    @NotNull
    @Min(0)
    private Integer stock;

    @NotNull
    private Boolean activo;

    @NotNull
    private Long categoriaId;
}