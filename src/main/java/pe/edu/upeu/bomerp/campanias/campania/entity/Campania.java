package pe.edu.upeu.bomerp.campanias.campania.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "CAMPANIAS", schema = "BOM_CAMPANIAS")
@Getter
@Setter
@NoArgsConstructor
public class Campania {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE", nullable = false, length = 120)
    private String nombre;

    @Column(name = "FECHA_INICIO", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "FECHA_FIN", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "MAX_EVIDENCIAS_DIA", nullable = false)
    private Integer maxEvidenciasDia;

    @Column(name = "ESTADO", nullable = false, length = 20)
    private String estado;
}