
package pe.edu.upeu.bomerp.premios.premio.repository;

import pe.edu.upeu.bomerp.premios.premio.entity.Premio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PremioRepository extends JpaRepository<Premio, Long> {

    List<Premio> findByCategoriaId(Long categoriaId);

    boolean existsByCategoriaId(Long categoriaId);

    @Query("""
        SELECT p
        FROM Premio p
        JOIN FETCH p.categoria
        WHERE p.categoria.id = :categoriaId
        """)
    List<Premio> findByCategoriaIdConCategoria(
            @Param("categoriaId") Long categoriaId
    );

    @Query("""
        SELECT p
        FROM Premio p
        JOIN FETCH p.categoria
        """)
    List<Premio> findAllConCategoria();

    @Query("""
        SELECT p
        FROM Premio p
        JOIN FETCH p.categoria
        WHERE p.id = :id
        """)
    Optional<Premio> findByIdConCategoria(@Param("id") Long id);
}
