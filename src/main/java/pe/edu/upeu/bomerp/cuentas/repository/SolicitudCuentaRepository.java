package pe.edu.upeu.bomerp.cuentas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.cuentas.entity.EstadoSolicitud;
import pe.edu.upeu.bomerp.cuentas.entity.SolicitudCuenta;
import java.util.List;

public interface SolicitudCuentaRepository extends JpaRepository<SolicitudCuenta, Long> {

    List<SolicitudCuenta> findByEstadoOrderByFechaRevisionDesc(EstadoSolicitud estado);

    boolean existsByOdooLeadId(Long odooLeadId);
}
