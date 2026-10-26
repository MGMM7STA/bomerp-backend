package pe.edu.upeu.bomerp.premios.premio.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import pe.edu.upeu.bomerp.premios.categoria.entity.CategoriaPremio;

@Entity
@Table(name = "PREMIOS", schema = "BOM_PREMIOS")
@Getter
@Setter
@NoArgsConstructor
public class Premio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "DESCRIPCION", length = 200)
    private String descripcion;

    @Column(name = "STOCK", nullable = false)
    private Integer stock;

    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CATEGORIA_ID", nullable = false)
    private CategoriaPremio categoria;
}