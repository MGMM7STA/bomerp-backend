package pe.edu.upeu.bomerp.auditoria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "EVENTOS", schema = "BOM_AUDITORIA")
@Getter
@Setter
@NoArgsConstructor
public class EventoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "FECHA", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "USUARIO", nullable = false, length = 80)
    private String usuario;

    @Column(name = "NOMBRE", length = 150)
    private String nombre;

    @Column(name = "ACCION", nullable = false, length = 40)
    private String accion;

    @Column(name = "ENTIDAD", nullable = false, length = 40)
    private String entidad;

    @Column(name = "ENTIDAD_ID", length = 60)
    private String entidadId;

    @Column(name = "DETALLE", length = 500)
    private String detalle;

    @Column(name = "SEDE", length = 10)
    private String sede;

    @Column(name = "TRACE_ID", length = 64)
    private String traceId;
}
