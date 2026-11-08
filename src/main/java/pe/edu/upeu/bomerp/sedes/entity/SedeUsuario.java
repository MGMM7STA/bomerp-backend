package pe.edu.upeu.bomerp.sedes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.bomerp.sedes.Sede;

@Entity
@Table(name = "SEDES_USUARIO", schema = "BOM_PARTICIPACION")
@Getter
@Setter
@NoArgsConstructor
public class SedeUsuario {

    @Id
    @Column(name = "USUARIO", length = 80)
    private String usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "SEDE", nullable = false, length = 10)
    private Sede sede;
}
