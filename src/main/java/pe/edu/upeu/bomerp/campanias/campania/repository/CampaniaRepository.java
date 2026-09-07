package pe.edu.upeu.bomerp.campanias.campania.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.campanias.campania.entity.Campania;
import java.util.Optional;

public interface CampaniaRepository extends JpaRepository<Campania, Long> {
    Optional<Campania> findFirstByEstado(String estado);
}