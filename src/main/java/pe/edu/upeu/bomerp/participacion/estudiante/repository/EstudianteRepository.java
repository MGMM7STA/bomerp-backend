package pe.edu.upeu.bomerp.participacion.estudiante.repository;

import java.util.List;
import pe.edu.upeu.bomerp.sedes.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.participacion.estudiante.entity.Estudiante;
import java.util.Optional;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    Optional<Estudiante> findByKeycloakId(String keycloakId);

    Optional<Estudiante> findByUsuario(String usuario);

    List<Estudiante> findBySede(Sede sede);
}
