package pe.edu.upeu.bomerp.auditoria.dto;

import java.util.List;

public record PaginaEventos(
        List<EventoAuditoriaResponse> contenido,
        int pagina,
        int tamanio,
        long totalElementos,
        int totalPaginas,
        boolean primera,
        boolean ultima) {
}
