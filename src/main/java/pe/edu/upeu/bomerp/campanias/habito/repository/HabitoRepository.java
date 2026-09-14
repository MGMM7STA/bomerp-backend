package pe.edu.upeu.bomerp.campanias.habito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.campanias.habito.entity.Habito;
import java.util.List;

public interface HabitoRepository extends JpaRepository<Habito, Long> {

    List<Habito> findByCategoriaId(Long categoriaId);

    long countByCategoriaId(Long categoriaId);
}
