package pe.edu.upeu.bomerp.participacion.registro.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import pe.edu.upeu.bomerp.participacion.registro.dto.FotoEvidencia;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class FotoEvidenciaServiceImpl implements FotoEvidenciaService {

    private static final long TAMANIO_MAXIMO = 5L * 1024 * 1024;

    private static final Map<String, String> EXTENSIONES = Map.of(
            MediaType.IMAGE_JPEG_VALUE, ".jpg",
            MediaType.IMAGE_PNG_VALUE, ".png",
            "image/webp", ".webp");

    private final Path directorio;

    public FotoEvidenciaServiceImpl(@Value("${bomerp.evidencias.directorio}") String directorio) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
    }

    @Override
    public void verificar(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("La foto esta vacia");
        }
        if (archivo.getSize() > TAMANIO_MAXIMO) {
            throw new IllegalArgumentException("La foto pesa mas de 5 MB");
        }
        if (!EXTENSIONES.containsKey(archivo.getContentType())) {
            throw new IllegalArgumentException("La foto debe ser JPG, PNG o WEBP");
        }
    }

    @Override
    public String guardar(MultipartFile archivo) {
        verificar(archivo);
        String nombre = UUID.randomUUID() + EXTENSIONES.get(archivo.getContentType());
        try (InputStream entrada = archivo.getInputStream()) {
            Files.createDirectories(directorio);
            Files.copy(entrada, directorio.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la foto de la evidencia", e);
        }
        log.info("Foto de evidencia guardada como {}", nombre);
        return nombre;
    }

    @Override
    public FotoEvidencia leer(String nombre) {
        Path ruta = directorio.resolve(nombre).normalize();
        if (!ruta.startsWith(directorio) || !Files.isRegularFile(ruta)) {
            throw new ResourceNotFoundException("Foto no encontrada");
        }
        MediaType tipo = EXTENSIONES.entrySet().stream()
                .filter(par -> nombre.endsWith(par.getValue()))
                .map(par -> MediaType.parseMediaType(par.getKey()))
                .findFirst()
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return new FotoEvidencia(new PathResource(ruta), tipo);
    }
}
