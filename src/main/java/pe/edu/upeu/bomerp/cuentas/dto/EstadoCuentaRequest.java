package pe.edu.upeu.bomerp.cuentas.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstadoCuentaRequest {

    @NotNull
    private Boolean activa;
}
