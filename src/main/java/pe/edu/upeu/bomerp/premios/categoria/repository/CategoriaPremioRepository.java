package pe.edu.upeu.bomerp.premios.categoria.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.upeu.bomerp.premios.categoria.entity.CategoriaPremio;

public interface CategoriaPremioRepository extends JpaRepository<CategoriaPremio, Long> {
}