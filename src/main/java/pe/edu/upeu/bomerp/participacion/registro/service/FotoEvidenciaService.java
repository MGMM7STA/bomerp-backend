package pe.edu.upeu.bomerp.participacion.registro.service;

import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.bomerp.participacion.registro.dto.FotoEvidencia;

public interface FotoEvidenciaService {

    void verificar(MultipartFile archivo);

    String guardar(MultipartFile archivo);

    FotoEvidencia leer(String nombre);
}
