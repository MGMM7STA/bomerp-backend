package pe.edu.upeu.bomerp.participacion.estudiante.dto;

import pe.edu.upeu.bomerp.sedes.Sede;

public record NuevoEstudiante(
        String keycloakId,
        String usuario,
        String nombre,
        String correo,
        String codigoUniversitario,
        String dni,
        String escuela,
        Integer ciclo,
        String celular,
        Sede sede) {
}
