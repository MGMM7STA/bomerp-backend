package pe.edu.upeu.bomerp.participacion.estudiante.service;

import org.springframework.security.oauth2.jwt.Jwt;
import pe.edu.upeu.bomerp.participacion.estudiante.dto.EstudianteResponse;
import java.util.List;

public interface EstudianteService {

    EstudianteResponse resolver(Jwt jwt);

    List<EstudianteResponse> listar();
}
