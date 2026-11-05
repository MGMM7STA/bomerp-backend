package pe.edu.upeu.bomerp.campanias.campania.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.campanias.campania.entity.CampaniaHabito;
import java.util.List;

public interface CampaniaHabitoRepository extends JpaRepository<CampaniaHabito, Long> {

    List<CampaniaHabito> findByCampaniaId(Long campaniaId);

    boolean existsByCampaniaIdAndHabitoId(Long campaniaId, Long habitoId);

    void deleteByCampaniaId(Long campaniaId);
}
