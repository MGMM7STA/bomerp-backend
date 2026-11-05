package pe.edu.upeu.bomerp.participacion.estudiante.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.participacion.estudiante.dto.EstudianteResponse;
import pe.edu.upeu.bomerp.participacion.estudiante.entity.Estudiante;
import pe.edu.upeu.bomerp.participacion.estudiante.repository.EstudianteRepository;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstudianteServiceImpl implements EstudianteService {

    private final EstudianteRepository estudianteRepository;

    @Override
    @Transactional
    public EstudianteResponse resolver(Jwt jwt) {
        Estudiante estudiante = estudianteRepository.findByKeycloakId(jwt.getSubject())
                .orElseGet(() -> enlazar(jwt));
        return aResponse(estudiante);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteResponse> listar() {
        return estudianteRepository.findAll().stream().map(this::aResponse).toList();
    }

    private Estudiante enlazar(Jwt jwt) {
        String usuario = jwt.getClaimAsString("preferred_username");
        Estudiante estudiante = estudianteRepository.findByUsuario(usuario).orElseGet(Estudiante::new);
        estudiante.setKeycloakId(jwt.getSubject());
        estudiante.setUsuario(usuario);
        if (estudiante.getNombre() == null) {
            String nombre = jwt.getClaimAsString("name");
            estudiante.setNombre(nombre != null ? nombre : usuario);
        }
        if (estudiante.getCorreo() == null) {
            estudiante.setCorreo(jwt.getClaimAsString("email"));
        }
        log.info("Enlazando usuario {} de Keycloak con el estudiante id={}", usuario, estudiante.getId());
        return estudianteRepository.save(estudiante);
    }

    private EstudianteResponse aResponse(Estudiante estudiante) {
        return EstudianteResponse.builder()
                .id(estudiante.getId())
                .usuario(estudiante.getUsuario())
                .nombre(estudiante.getNombre())
                .correo(estudiante.getCorreo())
                .build();
    }
}
