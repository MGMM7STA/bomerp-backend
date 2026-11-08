package pe.edu.upeu.bomerp.cuentas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AprobacionRequest {

    @NotBlank
    @Size(min = 3, max = 60)
    @Pattern(regexp = "[a-z0-9._-]+", message = "solo minusculas, numeros, punto, guion y guion bajo")
    private String usuario;

    private boolean enviarCorreo = true;
}
