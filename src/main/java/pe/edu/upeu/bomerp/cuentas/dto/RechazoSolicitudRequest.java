package pe.edu.upeu.bomerp.cuentas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RechazoSolicitudRequest {

    @NotBlank
    @Size(min = 5, max = 300)
    private String motivo;
}
