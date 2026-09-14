package pe.edu.upeu.bomerp.campanias.campania.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class CampaniaRequest {

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @NotNull
    private LocalDate fechaInicio;

    @NotNull
    private LocalDate fechaFin;

    @NotNull
    @Positive
    private Integer maxEvidenciasDia;

    @NotBlank
    @Pattern(regexp = "ACTIVA|CERRADA|BORRADOR")
    private String estado;
}
