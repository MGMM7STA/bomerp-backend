package pe.edu.upeu.bomerp.participacion.estudiante.service;

import pe.edu.upeu.bomerp.sedes.SedeService;
import pe.edu.upeu.bomerp.sedes.Sede;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.participacion.estudiante.dto.EstudianteResponse;
import pe.edu.upeu.bomerp.participacion.estudiante.dto.NuevoEstudiante;
import pe.edu.upeu.bomerp.participacion.estudiante.entity.Estudiante;
import pe.edu.upeu.bomerp.participacion.estudiante.repository.EstudianteRepository;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstudianteServiceImpl implements EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final SedeService sedeService;

    @Override
    @Transactional
    public EstudianteResponse resolver(Jwt jwt) {
        Estudiante estudiante = estudianteRepository.findByKeycloakId(jwt.getSubject())
                .orElseGet(() -> enlazar(jwt));
        if (estudiante.getSede() == null) {
            sedeService.de(estudiante.getUsuario()).ifPresent(sede -> {
                estudiante.setSede(sede);
                estudianteRepository.save(estudiante);
            });
        }
        return aResponse(estudiante);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteResponse> listar(Sede sede) {
        List<Estudiante> lista = sede == null ? estudianteRepository.findAll() : estudianteRepository.findBySede(sede);
        return lista.stream().map(this::aResponse).toList();
    }

    @Override
    @Transactional
    public void cambiarSede(String usuario, Sede sede) {
        estudianteRepository.findByUsuario(usuario).ifPresent(estudiante -> {
            estudiante.setSede(sede);
            estudianteRepository.save(estudiante);
        });
    }

    @Override
    @Transactional
    public EstudianteResponse registrar(NuevoEstudiante datos) {
        Estudiante estudiante = estudianteRepository.findByUsuario(datos.usuario()).orElseGet(Estudiante::new);
        estudiante.setKeycloakId(datos.keycloakId());
        estudiante.setUsuario(datos.usuario());
        estudiante.setNombre(datos.nombre());
        estudiante.setCorreo(datos.correo());
        estudiante.setCodigoUniversitario(datos.codigoUniversitario());
        estudiante.setDni(datos.dni());
        estudiante.setEscuela(datos.escuela());
        estudiante.setCiclo(datos.ciclo());
        estudiante.setCelular(datos.celular());
        estudiante.setSede(datos.sede());
        log.info("Registrando estudiante {} desde una solicitud aprobada", datos.usuario());
        return aResponse(estudianteRepository.save(estudiante));
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
                .codigoUniversitario(estudiante.getCodigoUniversitario())
                .dni(estudiante.getDni())
                .escuela(estudiante.getEscuela())
                .ciclo(estudiante.getCiclo())
                .celular(estudiante.getCelular())
                .sede(estudiante.getSede() == null ? null : estudiante.getSede().name())
                .build();
    }
}
