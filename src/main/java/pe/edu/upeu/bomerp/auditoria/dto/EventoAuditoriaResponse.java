package pe.edu.upeu.bomerp.auditoria.dto;

import pe.edu.upeu.bomerp.auditoria.entity.EventoAuditoria;
import java.time.LocalDateTime;

public record EventoAuditoriaResponse(
        Long id,
        LocalDateTime fecha,
        String usuario,
        String nombre,
        String accion,
        String entidad,
        String entidadId,
        String detalle,
        String sede,
        String traceId) {

    public static EventoAuditoriaResponse de(EventoAuditoria e) {
        return new EventoAuditoriaResponse(e.getId(), e.getFecha(), e.getUsuario(), e.getNombre(), e.getAccion(),
                e.getEntidad(), e.getEntidadId(), e.getDetalle(), e.getSede(), e.getTraceId());
    }
}
