package pe.edu.upeu.bomerp.campanias.habito.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class HabitoRequest {

    @NotNull
    private Long categoriaId;

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @Size(max = 255)
    private String descripcion;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal puntajeBase;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer activo;
}
