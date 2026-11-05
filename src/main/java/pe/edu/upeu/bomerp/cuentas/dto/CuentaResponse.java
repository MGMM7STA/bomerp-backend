package pe.edu.upeu.bomerp.cuentas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaResponse {
    private String id;
    private String usuario;
    private String nombres;
    private String apellidos;
    private String correo;
    private String rol;
    private Boolean activa;
    private Instant creadaEn;
}
