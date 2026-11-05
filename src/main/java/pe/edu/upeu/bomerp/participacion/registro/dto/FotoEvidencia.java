package pe.edu.upeu.bomerp.participacion.registro.dto;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record FotoEvidencia(Resource contenido, MediaType tipo) {
}
