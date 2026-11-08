package pe.edu.upeu.bomerp.cuentas.dto;

import pe.edu.upeu.bomerp.sedes.Sede;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolRequest {

    @NotBlank
    @Pattern(regexp = "ESTUDIANTE|COORDINADOR|ADMIN")
    private String rol;

    private Sede sede;
}
