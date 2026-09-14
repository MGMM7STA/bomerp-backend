package pe.edu.upeu.bomerp.participacion.registro.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class RegistroRequest {

    @NotNull
    private Long estudianteId;

    @NotEmpty
    @Valid
    private List<DetalleEvidenciaRequest> detalles;
}