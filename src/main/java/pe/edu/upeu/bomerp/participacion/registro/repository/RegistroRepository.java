package pe.edu.upeu.bomerp.participacion.registro.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroAgregado;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResumen;
import pe.edu.upeu.bomerp.participacion.registro.entity.EstadoRegistro;
import pe.edu.upeu.bomerp.participacion.registro.entity.RegistroParticipacion;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegistroRepository extends JpaRepository<RegistroParticipacion, Long> {

    @Override
    @EntityGraph(attributePaths = "detalles")
    List<RegistroParticipacion> findAll();

    @Override
    @EntityGraph(attributePaths = "detalles")
    Optional<RegistroParticipacion> findById(Long id);

    @EntityGraph(attributePaths = "detalles")
    @Query("""
        SELECT r FROM RegistroParticipacion r
        WHERE (:estado IS NULL OR r.estado = :estado)
          AND (:estudianteId IS NULL OR r.estudianteId = :estudianteId)
          AND (:desde IS NULL OR r.fecha >= :desde)
          AND (:hasta IS NULL OR r.fecha <= :hasta)
        """)
    List<RegistroParticipacion> buscar(@Param("estado") EstadoRegistro estado,
                                       @Param("estudianteId") Long estudianteId,
                                       @Param("desde") LocalDateTime desde,
                                       @Param("hasta") LocalDateTime hasta,
                                       Sort sort);

    @Query("""
        SELECT new pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResumen(
            r.id, r.estudianteId, r.fecha, r.estado, r.totalEvidencias, r.puntajeTotal, SIZE(r.detalles))
        FROM RegistroParticipacion r
        WHERE (:estado IS NULL OR r.estado = :estado)
          AND (:estudianteId IS NULL OR r.estudianteId = :estudianteId)
          AND (:desde IS NULL OR r.fecha >= :desde)
          AND (:hasta IS NULL OR r.fecha <= :hasta)
        """)
    List<RegistroResumen> buscarResumen(@Param("estado") EstadoRegistro estado,
                                        @Param("estudianteId") Long estudianteId,
                                        @Param("desde") LocalDateTime desde,
                                        @Param("hasta") LocalDateTime hasta,
                                        Sort sort);

    @Query("""
        SELECT new pe.edu.upeu.bomerp.participacion.registro.dto.RegistroAgregado(
            COUNT(r), COALESCE(SUM(r.totalEvidencias), 0L), COALESCE(SUM(r.puntajeTotal), 0BD))
        FROM RegistroParticipacion r
        WHERE (:estado IS NULL OR r.estado = :estado)
          AND (:estudianteId IS NULL OR r.estudianteId = :estudianteId)
          AND (:desde IS NULL OR r.fecha >= :desde)
          AND (:hasta IS NULL OR r.fecha <= :hasta)
        """)
    RegistroAgregado agregados(@Param("estado") EstadoRegistro estado,
                               @Param("estudianteId") Long estudianteId,
                               @Param("desde") LocalDateTime desde,
                               @Param("hasta") LocalDateTime hasta);
}
