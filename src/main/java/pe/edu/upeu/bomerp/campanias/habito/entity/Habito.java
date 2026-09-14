package pe.edu.upeu.bomerp.campanias.habito.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.bomerp.campanias.categoriahabito.entity.CategoriaHabito;
import java.math.BigDecimal;

@Entity
@Table(name = "HABITOS", schema = "BOM_CAMPANIAS")
@Getter
@Setter
@NoArgsConstructor
public class Habito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ID_CATEGORIA", nullable = false)
    private CategoriaHabito categoria;

    @Column(name = "NOMBRE", nullable = false, length = 120)
    private String nombre;

    @Column(name = "DESCRIPCION", length = 255)
    private String descripcion;

    @Column(name = "PUNTAJE_BASE", nullable = false, precision = 10, scale = 2)
    private BigDecimal puntajeBase;

    @Column(name = "ACTIVO", nullable = false)
    private Integer activo;
}
