package pe.edu.upeu.bomerp.premios.premiacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.upeu.bomerp.premios.premiacion.entity.ActaPremiacion;
import pe.edu.upeu.bomerp.sedes.Sede;

import java.time.LocalDate;
import java.util.List;

public interface ActaPremiacionRepository
        extends JpaRepository<ActaPremiacion, Long> {

    boolean existsByCampaniaIdAndSedeAndFechaInicioPeriodoAndFechaFinPeriodo(
            Long campaniaId,
            Sede sede,
            LocalDate fechaInicioPeriodo,
            LocalDate fechaFinPeriodo
    );

    List<ActaPremiacion> findAllByOrderByFechaEmisionDesc();

    List<ActaPremiacion> findBySedeOrderByFechaEmisionDesc(Sede sede);
}
