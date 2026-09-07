package pe.edu.upeu.bomerp.participacion.registro.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "REGISTROS", schema = "BOM_PARTICIPACION")
@Getter
@Setter
@NoArgsConstructor
public class RegistroParticipacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ID_ESTUDIANTE", nullable = false)
    private Long estudianteId;

    @Column(name = "ID_CAMPANIA", nullable = false)
    private Long campaniaId;

    @Column(name = "FECHA", nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoRegistro estado;

    @Column(name = "TOTAL_EVIDENCIAS", nullable = false)
    private Integer totalEvidencias;

    @Column(name = "PUNTAJE_TOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal puntajeTotal;

    @OneToMany(mappedBy = "registro", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleEvidencia> detalles = new ArrayList<>();
}