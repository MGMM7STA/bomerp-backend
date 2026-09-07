package pe.edu.upeu.bomerp.campanias.habito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.campanias.habito.entity.Habito;

public interface HabitoRepository extends JpaRepository<Habito, Long> {
}