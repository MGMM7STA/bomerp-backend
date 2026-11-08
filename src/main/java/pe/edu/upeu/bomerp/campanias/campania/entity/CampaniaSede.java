package pe.edu.upeu.bomerp.campanias.campania.entity;

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
import pe.edu.upeu.bomerp.sedes.Sede;

@Entity
@Table(name = "CAMPANIA_SEDES", schema = "BOM_CAMPANIAS")
@Getter
@Setter
@NoArgsConstructor
public class CampaniaSede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ID_CAMPANIA", nullable = false)
    private Long campaniaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "SEDE", nullable = false, length = 10)
    private Sede sede;
}
