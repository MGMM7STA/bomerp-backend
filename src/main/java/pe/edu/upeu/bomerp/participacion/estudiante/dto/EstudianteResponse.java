package pe.edu.upeu.bomerp.participacion.estudiante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstudianteResponse {
    private Long id;
    private String usuario;
    private String nombre;
    private String correo;
    private String codigoUniversitario;
    private String dni;
    private String escuela;
    private Integer ciclo;
    private String celular;
    private String sede;
}
