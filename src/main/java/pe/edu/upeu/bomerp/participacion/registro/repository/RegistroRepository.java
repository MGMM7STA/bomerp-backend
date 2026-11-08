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
import pe.edu.upeu.bomerp.sedes.Sede;
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
          AND (:sede IS NULL OR r.sede = :sede)
        """)
    List<RegistroParticipacion> buscar(@Param("estado") EstadoRegistro estado,
                                       @Param("estudianteId") Long estudianteId,
                                       @Param("desde") LocalDateTime desde,
                                       @Param("hasta") LocalDateTime hasta,
                                       @Param("sede") Sede sede,
                                       Sort sort);

    @Query("""
        SELECT new pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResumen(
            r.id, r.estudianteId, r.fecha, r.estado, r.totalEvidencias, r.puntajeTotal, SIZE(r.detalles))
        FROM RegistroParticipacion r
        WHERE (:estado IS NULL OR r.estado = :estado)
          AND (:estudianteId IS NULL OR r.estudianteId = :estudianteId)
          AND (:desde IS NULL OR r.fecha >= :desde)
          AND (:hasta IS NULL OR r.fecha <= :hasta)
          AND (:sede IS NULL OR r.sede = :sede)
        """)
    List<RegistroResumen> buscarResumen(@Param("estado") EstadoRegistro estado,
                                        @Param("estudianteId") Long estudianteId,
                                        @Param("desde") LocalDateTime desde,
                                        @Param("hasta") LocalDateTime hasta,
                                        @Param("sede") Sede sede,
                                        Sort sort);

    @Query("""
        SELECT r.estudianteId, COALESCE(SUM(d.subtotal), 0BD), COALESCE(SUM(d.veces), 0L)
        FROM RegistroParticipacion r JOIN r.detalles d
        WHERE d.estado = pe.edu.upeu.bomerp.participacion.registro.entity.EstadoRegistro.VALIDADO
          AND r.campaniaId = :campaniaId
        GROUP BY r.estudianteId
        """)
    List<Object[]> puntosValidadosPorEstudiante(@Param("campaniaId") Long campaniaId);

    @Query("""
        SELECT new pe.edu.upeu.bomerp.participacion.registro.dto.RegistroAgregado(
            COUNT(r), COALESCE(SUM(r.totalEvidencias), 0L), COALESCE(SUM(r.puntajeTotal), 0BD))
        FROM RegistroParticipacion r
        WHERE (:estado IS NULL OR r.estado = :estado)
          AND (:estudianteId IS NULL OR r.estudianteId = :estudianteId)
          AND (:desde IS NULL OR r.fecha >= :desde)
          AND (:hasta IS NULL OR r.fecha <= :hasta)
          AND (:sede IS NULL OR r.sede = :sede)
        """)
    RegistroAgregado agregados(@Param("estado") EstadoRegistro estado,
                               @Param("estudianteId") Long estudianteId,
                               @Param("desde") LocalDateTime desde,
                               @Param("hasta") LocalDateTime hasta,
                               @Param("sede") Sede sede);
}
