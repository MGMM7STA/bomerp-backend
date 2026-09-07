package pe.edu.upeu.bomerp.participacion.registro.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.participacion.registro.entity.RegistroParticipacion;
import java.util.List;
import java.util.Optional;

public interface RegistroRepository extends JpaRepository<RegistroParticipacion, Long> {

    @Override
    @EntityGraph(attributePaths = "detalles")
    List<RegistroParticipacion> findAll();

    @Override
    @EntityGraph(attributePaths = "detalles")
    Optional<RegistroParticipacion> findById(Long id);
}