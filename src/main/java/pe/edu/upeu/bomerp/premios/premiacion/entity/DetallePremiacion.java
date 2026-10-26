package pe.edu.upeu.bomerp.premios.premiacion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "DETALLES_PREMIACION", schema = "BOM_PREMIOS")
public class DetallePremiacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ACTA_ID", nullable = false)
    private ActaPremiacion acta;

    @Column(name = "ESTUDIANTE_ID", nullable = false)
    private Long estudianteId;

    @Column(name = "POSICION", nullable = false)
    private Integer posicion;

    @Column(name = "PREMIO_ID", nullable = false)
    private Long premioId;

    @Column(name = "CANTIDAD", nullable = false)
    private Integer cantidad;

    @Column(name = "PUNTAJE_ALCANZADO", nullable = false)
    private Integer puntajeAlcanzado;
}