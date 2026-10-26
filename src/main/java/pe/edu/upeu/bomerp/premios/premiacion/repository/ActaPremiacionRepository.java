package pe.edu.upeu.bomerp.premios.premiacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.upeu.bomerp.premios.premiacion.entity.ActaPremiacion;

public interface ActaPremiacionRepository
        extends JpaRepository<ActaPremiacion, Long> {

    boolean existsByCampaniaIdAndFechaInicioPeriodoAndFechaFinPeriodo(
            Long campaniaId,
            java.time.LocalDate fechaInicioPeriodo,
            java.time.LocalDate fechaFinPeriodo
    );
}