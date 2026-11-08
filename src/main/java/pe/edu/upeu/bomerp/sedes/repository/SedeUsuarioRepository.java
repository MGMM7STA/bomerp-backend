package pe.edu.upeu.bomerp.sedes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.sedes.entity.SedeUsuario;

public interface SedeUsuarioRepository extends JpaRepository<SedeUsuario, String> {
}
