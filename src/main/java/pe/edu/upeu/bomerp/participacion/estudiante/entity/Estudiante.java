package pe.edu.upeu.bomerp.participacion.estudiante.entity;

import pe.edu.upeu.bomerp.sedes.Sede;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
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
@Table(name = "ESTUDIANTES", schema = "BOM_PARTICIPACION")
@Getter
@Setter
@NoArgsConstructor
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "KEYCLOAK_ID", length = 36, unique = true)
    private String keycloakId;

    @Column(name = "USUARIO", nullable = false, length = 80, unique = true)
    private String usuario;

    @Column(name = "NOMBRE", nullable = false, length = 150)
    private String nombre;

    @Column(name = "CORREO", length = 150)
    private String correo;

    @Column(name = "CODIGO_UNIVERSITARIO", length = 20)
    private String codigoUniversitario;

    @Column(name = "DNI", length = 8)
    private String dni;

    @Column(name = "ESCUELA", length = 120)
    private String escuela;

    @Column(name = "CICLO")
    private Integer ciclo;

    @Column(name = "CELULAR", length = 15)
    private String celular;

    @Enumerated(EnumType.STRING)
    @Column(name = "SEDE", length = 10)
    private Sede sede;
}
