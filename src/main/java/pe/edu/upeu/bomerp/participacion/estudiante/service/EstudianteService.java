package pe.edu.upeu.bomerp.participacion.estudiante.service;

import pe.edu.upeu.bomerp.sedes.Sede;
import org.springframework.security.oauth2.jwt.Jwt;
import pe.edu.upeu.bomerp.participacion.estudiante.dto.EstudianteResponse;
import pe.edu.upeu.bomerp.participacion.estudiante.dto.NuevoEstudiante;
import java.util.List;

public interface EstudianteService {

    EstudianteResponse resolver(Jwt jwt);

    List<EstudianteResponse> listar(Sede sede);

    void cambiarSede(String usuario, Sede sede);

    EstudianteResponse registrar(NuevoEstudiante datos);
}
