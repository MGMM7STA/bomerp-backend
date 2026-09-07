package pe.edu.upeu.bomerp.campanias.campania.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.campanias.campania.entity.CupoDiario;
import java.time.LocalDate;
import java.util.Optional;

public interface CupoDiarioRepository extends JpaRepository<CupoDiario, Long> {
    Optional<CupoDiario> findByCampaniaIdAndEstudianteIdAndFecha(Long campaniaId, Long estudianteId, LocalDate fecha);
}