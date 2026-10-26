package pe.edu.upeu.bomerp.premios.premiacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.upeu.bomerp.premios.premiacion.entity.DetallePremiacion;

import java.util.List;

public interface DetallePremiacionRepository
        extends JpaRepository<DetallePremiacion, Long> {

    List<DetallePremiacion> findByActaId(Long actaId);
}