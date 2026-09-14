package pe.edu.upeu.bomerp.participacion.registro.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class RegistroReporte {
    private final RegistroAgregado agregado;
    private final List<RegistroResumen> registros;
}
