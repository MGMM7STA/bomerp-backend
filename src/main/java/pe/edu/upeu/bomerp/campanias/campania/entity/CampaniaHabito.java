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

@Entity
@Table(name = "CAMPANIA_HABITOS", schema = "BOM_CAMPANIAS")
@Getter
@Setter
@NoArgsConstructor
public class CampaniaHabito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ID_CAMPANIA", nullable = false)
    private Long campaniaId;

    @Column(name = "ID_HABITO", nullable = false)
    private Long habitoId;
}
