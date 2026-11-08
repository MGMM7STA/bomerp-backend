package pe.edu.upeu.bomerp.campanias.campania.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.campanias.campania.entity.CampaniaSede;
import pe.edu.upeu.bomerp.sedes.Sede;
import java.util.List;

public interface CampaniaSedeRepository extends JpaRepository<CampaniaSede, Long> {

    List<CampaniaSede> findByCampaniaId(Long campaniaId);

    List<CampaniaSede> findBySede(Sede sede);

    void deleteByCampaniaId(Long campaniaId);
}
