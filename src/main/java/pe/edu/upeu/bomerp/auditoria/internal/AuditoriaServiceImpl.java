package pe.edu.upeu.bomerp.auditoria.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.auditoria.AuditoriaService;
import pe.edu.upeu.bomerp.auditoria.dto.EventoAuditoriaResponse;
import pe.edu.upeu.bomerp.auditoria.dto.PaginaEventos;
import pe.edu.upeu.bomerp.auditoria.entity.EventoAuditoria;
import pe.edu.upeu.bomerp.auditoria.repository.EventoAuditoriaRepository;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {

    private final EventoAuditoriaRepository repository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Jwt actor, String accion, String entidad, Object entidadId, String detalle, String sede) {
        try {
            EventoAuditoria evento = new EventoAuditoria();
            evento.setFecha(LocalDateTime.now());
            evento.setUsuario(corto(actor == null ? "sistema" : actor.getClaimAsString("preferred_username"), 80));
            evento.setNombre(corto(actor == null ? null : actor.getClaimAsString("name"), 150));
            evento.setAccion(accion);
            evento.setEntidad(entidad);
            evento.setEntidadId(entidadId == null ? null : corto(String.valueOf(entidadId), 60));
            evento.setDetalle(corto(detalle, 500));
            evento.setSede(sede);
            evento.setTraceId(corto(MDC.get("traceId"), 64));
            repository.save(evento);
            log.info("Auditoria {} sobre {} {} por {}", accion, entidad, entidadId, evento.getUsuario());
        } catch (RuntimeException e) {
            log.warn("No se pudo registrar la auditoria {} sobre {} {}: {}", accion, entidad, entidadId, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PaginaEventos buscar(String accion, String usuario, String sede, int pagina, int tamanio) {
        Page<EventoAuditoria> resultado = repository.buscar(vacioANulo(accion), vacioANulo(usuario), vacioANulo(sede),
                PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanio, 1), 100),
                        Sort.by(Sort.Direction.DESC, "fecha", "id")));
        return new PaginaEventos(
                resultado.getContent().stream().map(EventoAuditoriaResponse::de).toList(),
                resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(),
                resultado.getTotalPages(), resultado.isFirst(), resultado.isLast());
    }

    @Transactional(readOnly = true)
    public List<String> acciones() {
        return repository.acciones();
    }

    private String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private String corto(String valor, int maximo) {
        if (valor == null) {
            return null;
        }
        return valor.length() <= maximo ? valor : valor.substring(0, maximo);
    }
}
