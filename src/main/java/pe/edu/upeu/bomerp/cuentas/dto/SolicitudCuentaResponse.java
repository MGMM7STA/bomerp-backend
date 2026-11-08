package pe.edu.upeu.bomerp.cuentas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCuentaResponse {
    private Long id;
    private String nombres;
    private String apellidos;
    private String correo;
    private String codigoUniversitario;
    private String dni;
    private String escuela;
    private Integer ciclo;
    private String celular;
    private String sede;
    private Boolean tieneCarne;
    private String estado;
    private String motivoRechazo;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRevision;
    private String revisadoPor;
    private String usuarioCreado;
}
