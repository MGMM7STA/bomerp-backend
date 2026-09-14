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
@Table(name = "CUPOS_DIARIOS", schema = "BOM_CAMPANIAS")
@Getter
@Setter
@NoArgsConstructor
public class CupoDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ID_CAMPANIA", nullable = false)
    private Long campaniaId;

    @Column(name = "ID_ESTUDIANTE", nullable = false)
    private Long estudianteId;

    @Column(name = "FECHA", nullable = false)
    private LocalDate fecha;

    @Column(name = "EVIDENCIAS_USADAS", nullable = false)
    private Integer evidenciasUsadas;
}