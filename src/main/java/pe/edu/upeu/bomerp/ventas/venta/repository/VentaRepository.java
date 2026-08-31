package pe.edu.upeu.bomerp.ventas.venta.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.ventas.venta.entity.Venta;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Override
    @EntityGraph(attributePaths = "detalles")
    List<Venta> findAll();

    @Override
    @EntityGraph(attributePaths = "detalles")
    Optional<Venta> findById(Long id);
}