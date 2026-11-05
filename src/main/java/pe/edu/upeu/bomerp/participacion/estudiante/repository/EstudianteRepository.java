package pe.edu.upeu.bomerp.participacion.estudiante.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.participacion.estudiante.entity.Estudiante;
import java.util.Optional;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    Optional<Estudiante> findByKeycloakId(String keycloakId);

    Optional<Estudiante> findByUsuario(String usuario);
}
