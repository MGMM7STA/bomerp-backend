package pe.edu.upeu.bomerp.cuentas.dto;

import pe.edu.upeu.bomerp.sedes.Sede;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CuentaRequest {

    @NotBlank
    @Size(min = 3, max = 60)
    @Pattern(regexp = "[a-z0-9._-]+", message = "solo minusculas, numeros, punto, guion y guion bajo")
    private String usuario;

    @NotBlank
    @Size(max = 80)
    private String nombres;

    @NotBlank
    @Size(max = 80)
    private String apellidos;

    @NotBlank
    @Email
    @Size(max = 120)
    private String correo;

    @NotBlank
    @Pattern(regexp = "ESTUDIANTE|COORDINADOR|ADMIN")
    private String rol;

    @NotBlank
    @Size(min = 8, max = 64)
    private String contrasenia;

    private Sede sede;
}
