package pe.edu.upeu.bomerp.premios.premiacion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "ACTAS_PREMIACION",
    schema = "BOM_PREMIOS",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_ACTA_CAMPANIA_PERIODO",
            columnNames = {
                "CAMPANIA_ID",
                "FECHA_INICIO_PERIODO",
                "FECHA_FIN_PERIODO"
            }
        )
    }
)
public class ActaPremiacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CAMPANIA_ID", nullable = false)
    private Long campaniaId;

    @Column(name = "FECHA_INICIO_PERIODO", nullable = false)
    private LocalDate fechaInicioPeriodo;

    @Column(name = "FECHA_FIN_PERIODO", nullable = false)
    private LocalDate fechaFinPeriodo;

    @Column(name = "FECHA_EMISION", nullable = false)
    private LocalDateTime fechaEmision;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoActaPremiacion estado;

    @Column(name = "TOTAL_GANADORES", nullable = false)
    private Integer totalGanadores;

    @Column(name = "TOTAL_PREMIOS_ENTREGADOS", nullable = false)
    private Integer totalPremiosEntregados;

    @OneToMany(
        mappedBy = "acta",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<DetallePremiacion> detalles = new ArrayList<>();
}