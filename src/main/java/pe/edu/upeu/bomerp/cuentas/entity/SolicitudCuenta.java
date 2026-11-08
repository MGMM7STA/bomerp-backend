package pe.edu.upeu.bomerp.cuentas.entity;

import pe.edu.upeu.bomerp.sedes.Sede;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "SOLICITUDES_CUENTA", schema = "BOM_PARTICIPACION")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudCuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ODOO_LEAD_ID")
    private Long odooLeadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "SEDE", length = 10)
    private Sede sede;

    @Column(name = "NOMBRES", nullable = false, length = 80)
    private String nombres;

    @Column(name = "APELLIDOS", nullable = false, length = 80)
    private String apellidos;

    @Column(name = "CORREO", nullable = false, length = 120)
    private String correo;

    @Column(name = "CODIGO_UNIVERSITARIO", length = 20)
    private String codigoUniversitario;

    @Column(name = "DNI", length = 8)
    private String dni;

    @Column(name = "ESCUELA", length = 120)
    private String escuela;

    @Column(name = "CICLO")
    private Integer ciclo;

    @Column(name = "CELULAR", length = 15)
    private String celular;

    @Column(name = "ARCHIVO_CARNE", length = 120)
    private String archivoCarne;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(name = "MOTIVO_RECHAZO", length = 300)
    private String motivoRechazo;

    @Column(name = "FECHA_SOLICITUD", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "FECHA_REVISION")
    private LocalDateTime fechaRevision;

    @Column(name = "REVISADO_POR", length = 150)
    private String revisadoPor;

    @Column(name = "USUARIO_CREADO", length = 80)
    private String usuarioCreado;
}
