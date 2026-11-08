package pe.edu.upeu.bomerp.cuentas.dto;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record ArchivoCarne(Resource contenido, MediaType tipo) {
}
