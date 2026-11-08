package pe.edu.upeu.bomerp.auditoria;

import org.springframework.security.oauth2.jwt.Jwt;

public interface AuditoriaService {

    void registrar(Jwt actor, String accion, String entidad, Object entidadId, String detalle, String sede);
}
