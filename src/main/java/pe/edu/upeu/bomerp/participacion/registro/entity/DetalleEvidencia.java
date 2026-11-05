package pe.edu.upeu.bomerp.participacion.registro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "DETALLE_EVIDENCIAS", schema = "BOM_PARTICIPACION")
@Getter
@Setter
@NoArgsConstructor
public class DetalleEvidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_REGISTRO", nullable = false)
    private RegistroParticipacion registro;

    @Column(name = "ID_HABITO", nullable = false)
    private Long habitoId;

    @Column(name = "NOMBRE_HABITO", nullable = false, length = 120)
    private String nombreHabito;

    @Column(name = "DESCRIPCION", length = 255)
    private String descripcion;

    @Column(name = "URL_IMAGEN", length = 255)
    private String urlImagen;

    @Column(name = "PUNTAJE_UNITARIO", nullable = false, precision = 10, scale = 2)
    private BigDecimal puntajeUnitario;

    @Column(name = "VECES", nullable = false)
    private Integer veces;

    @Column(name = "SUBTOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoRegistro estado = EstadoRegistro.ENVIADO;

    @Column(name = "MOTIVO_RECHAZO", length = 300)
    private String motivoRechazo;

    @Column(name = "ARCHIVO_IMAGEN", length = 120)
    private String archivoImagen;
}