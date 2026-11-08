package pe.edu.upeu.bomerp.auditoria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.bomerp.auditoria.entity.EventoAuditoria;
import java.util.List;

public interface EventoAuditoriaRepository extends JpaRepository<EventoAuditoria, Long> {

    @Query("""
        SELECT e FROM EventoAuditoria e
        WHERE (:accion IS NULL OR e.accion = :accion)
          AND (:usuario IS NULL OR LOWER(e.usuario) LIKE LOWER(CONCAT('%', :usuario, '%'))
               OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :usuario, '%')))
          AND (:sede IS NULL OR e.sede = :sede)
        """)
    Page<EventoAuditoria> buscar(@Param("accion") String accion,
                                 @Param("usuario") String usuario,
                                 @Param("sede") String sede,
                                 Pageable pageable);

    @Query("SELECT DISTINCT e.accion FROM EventoAuditoria e ORDER BY e.accion")
    List<String> acciones();
}
