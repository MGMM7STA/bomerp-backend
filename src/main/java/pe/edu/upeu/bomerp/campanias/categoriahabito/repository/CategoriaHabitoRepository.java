package pe.edu.upeu.bomerp.campanias.categoriahabito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.campanias.categoriahabito.entity.CategoriaHabito;
import java.util.Optional;

public interface CategoriaHabitoRepository extends JpaRepository<CategoriaHabito, Long> {
    Optional<CategoriaHabito> findByNombreIgnoreCase(String nombre);
}
