package pe.edu.upeu.bomerp.participacion.registro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RechazoRequest {

    @NotBlank
    @Size(max = 300)
    private String motivo;
}
