package pe.edu.upeu.bomerp.cuentas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;
import pe.edu.upeu.bomerp.cuentas.dto.AprobacionRequest;
import pe.edu.upeu.bomerp.cuentas.dto.AprobacionResponse;
import pe.edu.upeu.bomerp.cuentas.dto.ArchivoCarne;
import pe.edu.upeu.bomerp.cuentas.dto.CuentaRequest;
import pe.edu.upeu.bomerp.cuentas.dto.CuentaResponse;
import pe.edu.upeu.bomerp.cuentas.dto.SolicitudCuentaResponse;
import pe.edu.upeu.bomerp.cuentas.entity.EstadoSolicitud;
import pe.edu.upeu.bomerp.cuentas.entity.SolicitudCuenta;
import pe.edu.upeu.bomerp.cuentas.repository.SolicitudCuentaRepository;
import pe.edu.upeu.bomerp.exception.EstadoRegistroInvalidoException;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import pe.edu.upeu.bomerp.participacion.estudiante.dto.NuevoEstudiante;
import pe.edu.upeu.bomerp.participacion.estudiante.service.EstudianteService;
import pe.edu.upeu.bomerp.sedes.Sede;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitudCuentaServiceImpl implements SolicitudCuentaService {

    private static final String MODELO = "crm.lead";
    private static final List<String> CAMPOS = List.of("contact_name", "email_from", "phone", "x_nombres",
            "x_apellidos", "x_codigo", "x_dni", "x_escuela", "x_ciclo", "x_sede", "x_estado_solicitud",
            "x_motivo_rechazo", "x_usuario", "create_date");
    private static final String CARACTERES = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final SecureRandom AZAR = new SecureRandom();
    private static final DateTimeFormatter FECHA_ODOO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    private final OdooCliente odoo;
    private final SolicitudCuentaRepository solicitudRepository;
    private final CuentaService cuentaService;
    private final EstudianteService estudianteService;

    @Value("${bomerp.cuentas.dominio-correo:}")
    private String dominioCorreo;

    @Value("${bomerp.cuentas.url-web:http://localhost:4200/}")
    private String urlWeb;

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudCuentaResponse> listar(EstadoSolicitud estado) {
        if (estado != null && estado != EstadoSolicitud.PENDIENTE) {
            return solicitudRepository.findByEstadoOrderByFechaRevisionDesc(estado).stream()
                    .map(this::aResponse)
                    .toList();
        }
        List<Object> dominio = List.of(
                List.of("type", "=", "lead"),
                List.of("x_sede", "!=", false),
                List.of("x_estado_solicitud", "in", Arrays.asList(false, "pendiente")));
        List<Map<String, Object>> leads = odoo.buscar(MODELO, dominio, CAMPOS, "create_date asc", 200);
        Set<Long> conCarne = leadsConCarne(leads.stream().map(l -> ((Number) l.get("id")).longValue()).toList());
        List<SolicitudCuentaResponse> respuesta = new ArrayList<>();
        for (Map<String, Object> lead : leads) {
            SolicitudCuentaResponse s = desdeOdoo(lead);
            s.setTieneCarne(conCarne.contains(s.getId()));
            respuesta.add(s);
        }
        return respuesta;
    }

    @Override
    @Transactional
    public AprobacionResponse aprobar(Long leadId, AprobacionRequest request, String revisor) {
        SolicitudCuentaResponse datos = pendiente(leadId);
        if (datos.getSede() == null) {
            throw new IllegalArgumentException("La solicitud no tiene sede. Completa el campo Sede del lead en Odoo.");
        }
        if (datos.getNombres().isBlank() || datos.getApellidos().isBlank() || datos.getCorreo().isBlank()) {
            throw new IllegalArgumentException("A la solicitud le faltan nombres, apellidos o correo en Odoo.");
        }
        if (!dominioCorreo.isBlank() && !datos.getCorreo().endsWith("@" + dominioCorreo)) {
            throw new IllegalArgumentException("El correo " + datos.getCorreo() + " no es institucional (@"
                    + dominioCorreo + "). Rechaza la solicitud o corrige el correo en Odoo.");
        }
        String contrasenia = generarContrasenia();
        CuentaRequest cuentaRequest = new CuentaRequest();
        cuentaRequest.setUsuario(request.getUsuario().trim());
        cuentaRequest.setNombres(datos.getNombres());
        cuentaRequest.setApellidos(datos.getApellidos());
        cuentaRequest.setCorreo(datos.getCorreo());
        cuentaRequest.setRol("ESTUDIANTE");
        cuentaRequest.setSede(Sede.valueOf(datos.getSede()));
        cuentaRequest.setContrasenia(contrasenia);
        CuentaResponse cuenta = cuentaService.crear(cuentaRequest);

        SolicitudCuenta registro;
        try {
            estudianteService.registrar(new NuevoEstudiante(
                    cuenta.getId(), cuenta.getUsuario(), datos.getNombres() + " " + datos.getApellidos(),
                    datos.getCorreo(), datos.getCodigoUniversitario(), datos.getDni(), datos.getEscuela(),
                    datos.getCiclo(), datos.getCelular(), Sede.valueOf(datos.getSede())));
            registro = auditar(datos, EstadoSolicitud.APROBADA, null, revisor, cuenta.getUsuario());
            odoo.escribir(MODELO, leadId.intValue(), Map.of(
                    "x_estado_solicitud", "aprobada",
                    "x_usuario", cuenta.getUsuario()));
        } catch (RuntimeException e) {
            cuentaService.eliminar(cuenta.getId());
            throw e;
        }

        boolean enviado = request.isEnviarCorreo() && enviarCorreo(leadId, datos.getCorreo(),
                "Tu cuenta de Semestre Saludable está lista",
                correoAprobado(datos.getNombres(), cuenta.getUsuario(), contrasenia));
        nota(leadId, "Cuenta creada en Semestre Saludable con el usuario " + cuenta.getUsuario()
                + " por " + revisor + (enviado ? ". Se envió el correo con sus datos de acceso." : "."));
        log.info("Solicitud Odoo {} aprobada: cuenta {}, correo enviado={}", leadId, cuenta.getUsuario(), enviado);
        return AprobacionResponse.builder()
                .solicitud(aResponse(registro))
                .cuenta(cuenta)
                .correoEnviado(enviado)
                .contraseniaTemporal(enviado ? null : contrasenia)
                .build();
    }

    @Override
    @Transactional
    public SolicitudCuentaResponse rechazar(Long leadId, String motivo, String revisor) {
        SolicitudCuentaResponse datos = pendiente(leadId);
        String limpio = motivo.trim();
        odoo.escribir(MODELO, leadId.intValue(), Map.of(
                "x_estado_solicitud", "rechazada",
                "x_motivo_rechazo", limpio));
        SolicitudCuenta registro = auditar(datos, EstadoSolicitud.RECHAZADA, limpio, revisor, null);
        if (!datos.getCorreo().isBlank()) {
            enviarCorreo(leadId, datos.getCorreo(), "Sobre tu solicitud de cuenta en Semestre Saludable",
                    correoRechazado(datos.getNombres(), limpio));
        }
        nota(leadId, "Solicitud rechazada por " + revisor + ". Motivo: " + limpio);
        return aResponse(registro);
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoCarne carne(Long leadId) {
        List<Map<String, Object>> adjuntos = odoo.buscar("ir.attachment", List.of(
                        List.of("res_model", "=", MODELO),
                        List.of("res_id", "=", leadId),
                        List.of("mimetype", "like", "image/")),
                List.of("datas", "mimetype"), "id desc", 1);
        if (adjuntos.isEmpty()) {
            throw new ResourceNotFoundException("La solicitud " + leadId + " no tiene foto del carné en Odoo");
        }
        byte[] contenido = odoo.decodificar(adjuntos.get(0).get("datas"));
        if (contenido == null) {
            throw new ResourceNotFoundException("La foto del carné de la solicitud " + leadId + " está vacía");
        }
        return new ArchivoCarne(new ByteArrayResource(contenido),
                MediaType.parseMediaType(texto(adjuntos.get(0).get("mimetype"), "image/jpeg")));
    }

    private SolicitudCuentaResponse pendiente(Long leadId) {
        if (solicitudRepository.existsByOdooLeadId(leadId)) {
            throw new EstadoRegistroInvalidoException("La solicitud " + leadId + " ya fue revisada");
        }
        SolicitudCuentaResponse datos = desdeOdoo(odoo.leer(MODELO, leadId.intValue(), CAMPOS));
        if (!"PENDIENTE".equals(datos.getEstado())) {
            throw new EstadoRegistroInvalidoException("La solicitud " + leadId + " ya fue revisada en Odoo");
        }
        return datos;
    }

    private Set<Long> leadsConCarne(List<Long> ids) {
        if (ids.isEmpty()) {
            return Set.of();
        }
        Set<Long> resultado = new HashSet<>();
        for (Map<String, Object> adjunto : odoo.buscar("ir.attachment", List.of(
                        List.of("res_model", "=", MODELO),
                        List.of("res_id", "in", ids),
                        List.of("mimetype", "like", "image/")),
                List.of("res_id"), null, null)) {
            resultado.add(((Number) adjunto.get("res_id")).longValue());
        }
        return resultado;
    }

    private SolicitudCuentaResponse desdeOdoo(Map<String, Object> lead) {
        String nombres = texto(lead.get("x_nombres"), "");
        String apellidos = texto(lead.get("x_apellidos"), "");
        if (nombres.isBlank() && apellidos.isBlank()) {
            String[] partes = texto(lead.get("contact_name"), "").trim().split("\\s+");
            int corte = partes.length >= 3 ? partes.length - 2 : Math.max(1, partes.length - 1);
            nombres = String.join(" ", Arrays.copyOfRange(partes, 0, Math.min(corte, partes.length)));
            apellidos = partes.length > corte ? String.join(" ", Arrays.copyOfRange(partes, corte, partes.length)) : "";
        }
        String estado = texto(lead.get("x_estado_solicitud"), "pendiente").toUpperCase();
        return SolicitudCuentaResponse.builder()
                .id(((Number) lead.get("id")).longValue())
                .nombres(nombres.trim())
                .apellidos(apellidos.trim())
                .correo(texto(lead.get("email_from"), "").trim().toLowerCase())
                .celular(texto(lead.get("phone"), ""))
                .codigoUniversitario(texto(lead.get("x_codigo"), ""))
                .dni(texto(lead.get("x_dni"), ""))
                .escuela(texto(lead.get("x_escuela"), ""))
                .ciclo(entero(lead.get("x_ciclo")))
                .sede(sede(lead.get("x_sede")))
                .tieneCarne(false)
                .estado(estado.equals("APROBADA") || estado.equals("RECHAZADA") ? estado : "PENDIENTE")
                .motivoRechazo(texto(lead.get("x_motivo_rechazo"), null))
                .usuarioCreado(texto(lead.get("x_usuario"), null))
                .fechaSolicitud(fecha(lead.get("create_date")))
                .build();
    }

    private SolicitudCuenta auditar(SolicitudCuentaResponse datos, EstadoSolicitud estado, String motivo,
                                    String revisor, String usuario) {
        SolicitudCuenta s = new SolicitudCuenta();
        s.setOdooLeadId(datos.getId());
        s.setNombres(corto(datos.getNombres().isBlank() ? "-" : datos.getNombres(), 80));
        s.setApellidos(corto(datos.getApellidos().isBlank() ? "-" : datos.getApellidos(), 80));
        s.setCorreo(corto(datos.getCorreo().isBlank() ? "-" : datos.getCorreo(), 120));
        s.setCodigoUniversitario(corto(datos.getCodigoUniversitario(), 20));
        s.setDni(corto(datos.getDni(), 8));
        s.setEscuela(corto(datos.getEscuela(), 120));
        s.setCiclo(datos.getCiclo());
        s.setCelular(corto(datos.getCelular(), 15));
        s.setSede(datos.getSede() == null ? null : Sede.valueOf(datos.getSede()));
        s.setEstado(estado);
        s.setMotivoRechazo(motivo);
        s.setFechaSolicitud(datos.getFechaSolicitud() != null ? datos.getFechaSolicitud() : LocalDateTime.now());
        s.setFechaRevision(LocalDateTime.now());
        s.setRevisadoPor(revisor);
        s.setUsuarioCreado(usuario);
        return solicitudRepository.save(s);
    }

    private boolean enviarCorreo(Long leadId, String para, String asunto, String html) {
        try {
            Map<String, Object> correo = new HashMap<>();
            correo.put("subject", asunto);
            correo.put("email_to", para);
            correo.put("body_html", html);
            correo.put("auto_delete", true);
            correo.put("model", MODELO);
            correo.put("res_id", leadId.intValue());
            int id = odoo.crear("mail.mail", correo);
            odoo.llamar("mail.mail", "send", List.of(List.of(id)), Map.of());
            List<Map<String, Object>> quedan = odoo.buscar("mail.mail", List.of(List.of("id", "=", id)),
                    List.of("state", "failure_reason"), null, 1);
            if (!quedan.isEmpty() && !"sent".equals(quedan.get(0).get("state"))) {
                log.warn("Odoo no pudo enviar el correo a {}: {}", para, quedan.get(0).get("failure_reason"));
                return false;
            }
            return true;
        } catch (RuntimeException e) {
            log.warn("No se pudo enviar el correo por Odoo a {}: {}", para, e.getMessage());
            return false;
        }
    }

    private void nota(Long leadId, String texto) {
        try {
            odoo.llamar(MODELO, "message_post", List.of(List.of(leadId.intValue())),
                    Map.of("body", texto, "message_type", "comment", "subtype_xmlid", "mail.mt_note"));
        } catch (RuntimeException e) {
            log.warn("No se pudo dejar la nota en el lead {}: {}", leadId, e.getMessage());
        }
    }

    private String correoAprobado(String nombres, String usuario, String contrasenia) {
        return plantilla("Tu cuenta está lista",
                "<p>Hola " + HtmlUtils.htmlEscape(nombres) + ",</p>"
                        + "<p>La Dirección de Universidad Saludable aprobó tu solicitud. Ya puedes registrar tus hábitos.</p>"
                        + "<table style=\"width:100%;border-collapse:collapse;margin:16px 0;background:#f4f6f8;border-radius:10px\">"
                        + "<tr><td style=\"padding:10px 14px;color:#6a7787;font-size:13px\">Usuario</td>"
                        + "<td style=\"padding:10px 14px;font-weight:700;color:#003866;font-family:monospace;font-size:15px\">"
                        + HtmlUtils.htmlEscape(usuario) + "</td></tr>"
                        + "<tr><td style=\"padding:10px 14px;color:#6a7787;font-size:13px\">Contraseña temporal</td>"
                        + "<td style=\"padding:10px 14px;font-weight:700;color:#003866;font-family:monospace;font-size:15px\">"
                        + HtmlUtils.htmlEscape(contrasenia) + "</td></tr></table>"
                        + "<p>Al ingresar por primera vez el sistema te pedirá cambiar la contraseña.</p>"
                        + "<p style=\"text-align:center;margin:22px 0\"><a href=\"" + HtmlUtils.htmlEscape(urlWeb)
                        + "\" style=\"background:#003866;color:#ffffff;text-decoration:none;padding:12px 22px;border-radius:10px;font-weight:600\">Ingresar a Semestre Saludable</a></p>");
    }

    private String correoRechazado(String nombres, String motivo) {
        return plantilla("Revisamos tu solicitud",
                "<p>Hola " + HtmlUtils.htmlEscape(nombres) + ",</p>"
                        + "<p>Por ahora no pudimos crear tu cuenta por este motivo:</p>"
                        + "<p style=\"padding:12px 14px;background:#fdecec;color:#8e1b1b;border-radius:10px\">"
                        + HtmlUtils.htmlEscape(motivo) + "</p>"
                        + "<p>Puedes enviar una nueva solicitud con los datos corregidos.</p>");
    }

    private String plantilla(String titulo, String cuerpo) {
        return "<div style=\"font-family:Arial,Helvetica,sans-serif;max-width:560px;margin:0 auto;color:#3a4652;font-size:15px;line-height:1.5\">"
                + "<div style=\"background:#003866;color:#ffffff;padding:18px 22px;border-radius:12px 12px 0 0\">"
                + "<div style=\"font-size:12px;letter-spacing:1.5px;text-transform:uppercase;color:#b2cc23;font-weight:700\">Semestre Saludable</div>"
                + "<div style=\"font-size:20px;font-weight:700;margin-top:4px\">" + HtmlUtils.htmlEscape(titulo) + "</div></div>"
                + "<div style=\"border:1px solid #e1e6ec;border-top:none;padding:20px 22px;border-radius:0 0 12px 12px\">"
                + cuerpo
                + "<p style=\"color:#6a7787;font-size:12px;margin-top:22px\">Dirección de Universidad Saludable · UPeU</p></div></div>";
    }

    private SolicitudCuentaResponse aResponse(SolicitudCuenta s) {
        return SolicitudCuentaResponse.builder()
                .id(s.getOdooLeadId() != null ? s.getOdooLeadId() : s.getId())
                .nombres(s.getNombres())
                .apellidos(s.getApellidos())
                .correo(s.getCorreo())
                .codigoUniversitario(s.getCodigoUniversitario())
                .dni(s.getDni())
                .escuela(s.getEscuela())
                .ciclo(s.getCiclo())
                .celular(s.getCelular())
                .sede(s.getSede() == null ? null : s.getSede().name())
                .tieneCarne(s.getOdooLeadId() != null)
                .estado(s.getEstado().name())
                .motivoRechazo(s.getMotivoRechazo())
                .fechaSolicitud(s.getFechaSolicitud())
                .fechaRevision(s.getFechaRevision())
                .revisadoPor(s.getRevisadoPor())
                .usuarioCreado(s.getUsuarioCreado())
                .build();
    }

    private String texto(Object valor, String porDefecto) {
        if (valor == null || Boolean.FALSE.equals(valor)) {
            return porDefecto;
        }
        String s = valor.toString();
        return s.isBlank() ? porDefecto : s;
    }

    private Integer entero(Object valor) {
        if (valor instanceof Number n) {
            return n.intValue() == 0 ? null : n.intValue();
        }
        String s = texto(valor, "").replaceAll("\\D", "");
        return s.isEmpty() ? null : Integer.valueOf(s);
    }

    private String sede(Object valor) {
        String s = texto(valor, "").trim().toUpperCase();
        for (Sede sede : Sede.values()) {
            if (sede.name().equals(s)) {
                return s;
            }
        }
        return null;
    }

    private LocalDateTime fecha(Object valor) {
        String s = texto(valor, null);
        if (s == null) {
            return null;
        }
        return LocalDateTime.parse(s.substring(0, 19), FECHA_ODOO)
                .atOffset(ZoneOffset.UTC).atZoneSameInstant(LIMA).toLocalDateTime();
    }

    private String corto(String valor, int maximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.length() <= maximo ? valor : valor.substring(0, maximo);
    }

    private String generarContrasenia() {
        StringBuilder clave = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            clave.append(CARACTERES.charAt(AZAR.nextInt(CARACTERES.length())));
        }
        return clave.toString();
    }
}
