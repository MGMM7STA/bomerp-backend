package pe.edu.upeu.bomerp.cuentas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContraseniaRequest {

    @NotBlank
    @Size(min = 8, max = 64)
    private String contrasenia;
}
