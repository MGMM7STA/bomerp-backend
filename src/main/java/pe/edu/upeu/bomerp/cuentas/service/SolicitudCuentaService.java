package pe.edu.upeu.bomerp.cuentas.service;

import pe.edu.upeu.bomerp.cuentas.dto.AprobacionRequest;
import pe.edu.upeu.bomerp.cuentas.dto.AprobacionResponse;
import pe.edu.upeu.bomerp.cuentas.dto.ArchivoCarne;
import pe.edu.upeu.bomerp.cuentas.dto.SolicitudCuentaResponse;
import pe.edu.upeu.bomerp.cuentas.entity.EstadoSolicitud;
import java.util.List;

public interface SolicitudCuentaService {

    List<SolicitudCuentaResponse> listar(EstadoSolicitud estado);

    AprobacionResponse aprobar(Long leadId, AprobacionRequest request, String revisor);

    SolicitudCuentaResponse rechazar(Long leadId, String motivo, String revisor);

    ArchivoCarne carne(Long leadId);
}
