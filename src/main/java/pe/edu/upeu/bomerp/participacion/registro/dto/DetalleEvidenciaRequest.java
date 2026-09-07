package pe.edu.upeu.bomerp.participacion.registro.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetalleEvidenciaRequest {

    @NotNull
    private Long habitoId;

    @NotNull
    @Positive
    private Integer veces;

    private String descripcion;

    private String urlImagen;
}